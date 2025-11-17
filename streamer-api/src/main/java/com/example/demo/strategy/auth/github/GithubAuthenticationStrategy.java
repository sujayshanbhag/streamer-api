package com.example.demo.strategy.auth.github;

import com.example.demo.constant.AuthenticationStatus;
import com.example.demo.strategy.auth.AuthenticationResult;
import com.example.demo.strategy.auth.BaseAuthenticationStrategy;
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

    @Override
    public AuthenticationResult authenticate(GithubAuthenticationInput input) {
        try {
            String token = input.getToken();
            GitHub github = new GitHubBuilder().withOAuthToken(token, clientId).build();

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
