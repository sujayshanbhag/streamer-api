package com.example.streamer.api.strategy.auth.otp;

import com.example.streamer.api.strategy.auth.AuthenticationResult;
import com.example.streamer.api.strategy.auth.BaseAuthenticationStrategy;
import org.springframework.stereotype.Service;

@Service
public class OtpAuthenticationStrategy extends BaseAuthenticationStrategy<OtpAuthenticationInput> {

    public OtpAuthenticationStrategy() {
        super(OtpAuthenticationInput.class);
    }
    @Override
    public AuthenticationResult authenticate(OtpAuthenticationInput input) {
        // TODO: Implement strategy based on OtpAuthenticationState
        throw new UnsupportedOperationException("createUser is not implemented yet");
    }
}
