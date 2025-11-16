package com.example.demo.strategy.auth.google;


import com.example.demo.strategy.auth.AuthenticationResult;
import com.example.demo.strategy.auth.BaseAuthenticationStrategy;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthenticationStrategy extends BaseAuthenticationStrategy<GoogleAuthenticationInput>  {

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthenticationStrategy(@Value("${oauth.google.client-id}") String clientId) {
        super(GoogleAuthenticationInput.class);
        verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    @Override
    public AuthenticationResult authenticate(GoogleAuthenticationInput input) {
        try {
            GoogleIdToken idToken = verifier.verify(input.getToken());
            if (idToken == null) {
                return AuthenticationResult.failed("Google auth failed");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String name = (String) payload.get("name");
            String email = payload.getEmail();

            return AuthenticationResult.builder()
                    .name(name)
                    .email(email)
                    .build();

        } catch (GeneralSecurityException | IOException e) {
            return AuthenticationResult.failed("Google auth failed");
        }
    }
}
