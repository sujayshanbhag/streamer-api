package com.courage.streamer.api.strategy.auth.google;

import com.courage.streamer.api.constant.AuthenticationType;
import com.courage.streamer.api.strategy.auth.Verifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleAuthenticationInput extends Verifiable {

    private String token;

    public GoogleAuthenticationInput() {
        super(AuthenticationType.GOOGLE);
    }

    @JsonCreator
    public GoogleAuthenticationInput(String token) {
        super(AuthenticationType.GOOGLE);
        this.token = token;
    }
}
