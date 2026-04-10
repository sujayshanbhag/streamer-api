package com.courage.streamer.api.strategy.auth.google;


import com.courage.streamer.api.constant.AuthenticationStatus;
import com.courage.streamer.api.strategy.auth.AuthenticationResult;
import com.courage.streamer.api.strategy.auth.BaseAuthenticationStrategy;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GoogleAuthenticationStrategy extends BaseAuthenticationStrategy<GoogleAuthenticationInput> {

    private final String clientId;
    private final String clientSecret;
    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthenticationStrategy(
            @Value("${oauth.google.client-id}") String clientId,
            @Value("${oauth.google.client-secret}") String clientSecret) {
        super(GoogleAuthenticationInput.class);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    protected GoogleIdToken exchangeCodeForIdToken(String code, String redirectUri) throws Exception {
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(), GsonFactory.getDefaultInstance(),
                clientId, clientSecret, code, redirectUri
        ).execute();
        GoogleIdToken idToken = tokenResponse.parseIdToken();
        return verifier.verify(idToken) ? idToken : null;
    }

    @Override
    public AuthenticationResult authenticate(GoogleAuthenticationInput input) {
        try {
            GoogleIdToken idToken = exchangeCodeForIdToken(input.getCode(), input.getRedirectUri());
            if (idToken == null) {
                return AuthenticationResult.failed("Google auth failed");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String name = (String) payload.get("name");
            String email = payload.getEmail();

            return AuthenticationResult.builder()
                    .name(name)
                    .email(email)
                    .status(AuthenticationStatus.SUCCESS)
                    .build();

        } catch (Exception e) {
            log.error("Google authentication failed", e);
            return AuthenticationResult.failed("Unknown Google auth error: " + e.getMessage());
        }
    }
}
