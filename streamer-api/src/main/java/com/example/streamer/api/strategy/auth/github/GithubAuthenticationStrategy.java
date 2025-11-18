package com.example.streamer.api.strategy.auth.github;

import com.example.streamer.api.constant.AuthenticationStatus;
import com.example.streamer.api.strategy.auth.AuthenticationResult;
import com.example.streamer.api.strategy.auth.BaseAuthenticationStrategy;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GithubAuthenticationStrategy extends BaseAuthenticationStrategy<GithubAuthenticationInput> {

    @Value("${oauth.github.client-id}")
    private String clientId;

    public GithubAuthenticationStrategy() {
        super(GithubAuthenticationInput.class);
    }

    protected GitHub createGithubClient(String token) throws IOException {
        return new GitHubBuilder().withOAuthToken(token, clientId).build();
    }

    @Override
    public AuthenticationResult authenticate(GithubAuthenticationInput input) {
        try {
            String token = input.getToken();
            GitHub github = createGithubClient(token);

            GHUser user = github.getMyself();

            return AuthenticationResult.builder()
                    .email(user.getEmail())
                    .name(user.getName())
                    .status(AuthenticationStatus.SUCCESS)
                    .build();
        } catch (IOException e) {
            return AuthenticationResult.failed("Unknown GitHub auth error");
        }
    }


}
