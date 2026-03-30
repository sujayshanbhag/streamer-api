package com.courage.streamer.api.strategy.auth.github;

import com.courage.streamer.api.constant.AuthenticationStatus;
import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import com.courage.streamer.api.strategy.auth.github.GithubAuthenticationInput;
import com.courage.streamer.api.strategy.auth.github.GithubAuthenticationStrategy;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GithubAuthenticationStrategyTest {

    private static final Map<String, String> MOCK_TOKEN_RESPONSE = Map.of("access_token", "mocked-token");

    private GithubAuthenticationStrategy buildStrategy(boolean failClient) {
        GithubAuthenticationStrategy strategy = new GithubAuthenticationStrategy() {
            @Override
            protected GitHub createGithubClient(String token) throws IOException {
                if (failClient) {
                    throw new IOException("API Error");
                }
                GHMyself mockUser = mock(GHMyself.class);
                when(mockUser.getEmail()).thenReturn("user@example.com");
                when(mockUser.getName()).thenReturn("Test User");

                GitHub mockGitHub = mock(GitHub.class);
                when(mockGitHub.getMyself()).thenReturn(mockUser);
                return mockGitHub;
            }
        };
        ReflectionTestUtils.setField(strategy, "clientId", "test-client-id");
        ReflectionTestUtils.setField(strategy, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(strategy, "redirectUri", "http://localhost/callback");
        return strategy;
    }

    @Test
    void testAuthenticate_success() throws Exception {
        GithubAuthenticationStrategy strategy = buildStrategy(false);
        GithubAuthenticationInput input = new GithubAuthenticationInput("mock-code");

        try (MockedConstruction<RestTemplate> ignored = Mockito.mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.postForObject(anyString(), any(), eq(Map.class)))
                        .thenReturn(MOCK_TOKEN_RESPONSE))) {

            AuthenticationResult result = strategy.authenticate(input);

            assertEquals(AuthenticationStatus.SUCCESS, result.getStatus());
            assertEquals("user@example.com", result.getEmail());
            assertEquals("Test User", result.getName());
        }
    }

    @Test
    void testAuthenticate_failure() throws Exception {
        GithubAuthenticationStrategy strategy = buildStrategy(true);
        GithubAuthenticationInput input = new GithubAuthenticationInput("bad-code");

        try (MockedConstruction<RestTemplate> ignored = Mockito.mockConstruction(RestTemplate.class,
                (mock, ctx) -> when(mock.postForObject(anyString(), any(), eq(Map.class)))
                        .thenReturn(MOCK_TOKEN_RESPONSE))) {

            AuthenticationResult result = strategy.authenticate(input);

            assertEquals(AuthenticationStatus.FAILED, result.getStatus());
            assertEquals("Unknown GitHub auth error", result.getMessage());
        }
    }
}
