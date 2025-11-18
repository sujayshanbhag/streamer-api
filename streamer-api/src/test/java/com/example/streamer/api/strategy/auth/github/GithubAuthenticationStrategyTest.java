package com.example.streamer.api.strategy.auth.github;

import com.example.streamer.api.constant.AuthenticationStatus;
import com.example.streamer.api.strategy.auth.AuthenticationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GithubAuthenticationStrategyTest {

    private GithubAuthenticationStrategy strategySuccess;
    private GithubAuthenticationStrategy strategyFailure;

    @BeforeEach
    void setUp() {
        strategySuccess = new GithubAuthenticationStrategy() {
            @Override
            protected GitHub createGithubClient(String token) throws IOException {
                GHMyself mockUser = mock(GHMyself.class);
                when(mockUser.getEmail()).thenReturn("user@example.com");
                when(mockUser.getName()).thenReturn("Test User");

                GitHub mockGitHub = mock(GitHub.class);
                when(mockGitHub.getMyself()).thenReturn(mockUser);
                return mockGitHub;
            }
        };

        // Failure GithubAuthenticationStrategy throwing IOException on client creation
        strategyFailure = new GithubAuthenticationStrategy() {
            @Override
            protected GitHub createGithubClient(String token) throws IOException {
                throw new IOException("API Error");
            }
        };
    }

    @Test
    void testAuthenticate_success() throws Exception {
        GithubAuthenticationInput input = new GithubAuthenticationInput("mock-token");

        AuthenticationResult result = strategySuccess.authenticate(input);

        assertEquals(AuthenticationStatus.SUCCESS, result.getStatus());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
    }

    @Test
    void testAuthenticate_failure() throws Exception {
        GithubAuthenticationInput input = new GithubAuthenticationInput("bad-token");

        AuthenticationResult result = strategyFailure.authenticate(input);

        assertEquals(AuthenticationStatus.FAILED, result.getStatus());
        assertEquals("Unknown GitHub auth error", result.getMessage());
    }
}
