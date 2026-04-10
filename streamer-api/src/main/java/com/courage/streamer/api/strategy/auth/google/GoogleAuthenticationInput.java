package com.courage.streamer.api.strategy.auth.google;

import com.courage.streamer.api.constant.AuthenticationType;
import com.courage.streamer.api.strategy.auth.Verifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleAuthenticationInput extends Verifiable {

    private String code;
    private String redirectUri;

    public GoogleAuthenticationInput() {
        super(AuthenticationType.GOOGLE);
    }

    @JsonCreator
    public GoogleAuthenticationInput(
            @JsonProperty("code") String code,
            @JsonProperty("redirectUri") String redirectUri) {
        super(AuthenticationType.GOOGLE);
        this.code = code;
        this.redirectUri = redirectUri;
    }
}
