package com.courage.streamer.api.strategy.auth.github;

import com.courage.streamer.api.constant.AuthenticationStatus;
import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import com.courage.streamer.api.strategy.auth.BaseAuthenticationStrategy;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GithubAuthenticationStrategy extends BaseAuthenticationStrategy<GithubAuthenticationInput> {

    @Value("${oauth.github.client-id}")
    private String clientId;

    @Value("${oauth.github.secret}")
    private String clientSecret;

    @Value("${oauth.github.redirect-uri}")
    private String redirectUri;

    public GithubAuthenticationStrategy() {
        super(GithubAuthenticationInput.class);
    }

    @Override
    public AuthenticationResult authenticate(GithubAuthenticationInput input) {
        try {
            String accessToken = exchangeCodeForToken(input.getAuthorizationCode());
            GitHub github = createGithubClient(accessToken);
            GHUser user = github.getMyself();
            return AuthenticationResult.builder()
                    .email(user.getEmail())
                    .name(user.getName())
                    .status(AuthenticationStatus.SUCCESS)
                    .build();
        } catch (IOException e) {
            log.error("GitHub authentication failed", e);
            return AuthenticationResult.failed("Unknown GitHub auth error");
        }
    }

    private String exchangeCodeForToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", code,
                "redirect_uri", redirectUri
        );

        Map<?, ?> response = restTemplate.postForObject(
                "https://github.com/login/oauth/access_token",
                new HttpEntity<>(body, headers),
                Map.class
        );

        String accessToken = response != null ? (String) response.get("access_token") : null;
        if (accessToken == null) {
            throw new RuntimeException("GitHub token exchange failed: " +
                    (response != null ? response.get("error_description") : "no response"));
        }
        return accessToken;
    }

    protected GitHub createGithubClient(String token) throws IOException {
        return new GitHubBuilder().withOAuthToken(token).build();
    }
}