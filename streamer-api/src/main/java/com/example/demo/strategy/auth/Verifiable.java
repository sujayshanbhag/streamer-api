package com.example.demo.strategy.auth;

import com.example.demo.constant.AuthenticationType;
import com.example.demo.strategy.auth.github.GithubAuthenticationInput;
import com.example.demo.strategy.auth.google.GoogleAuthenticationInput;
import com.example.demo.strategy.auth.otp.OtpAuthenticationInput;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @Type(value = GoogleAuthenticationInput.class, name = "GOOGLE"),
        @Type(value = OtpAuthenticationInput.class, name = "OTP"),
        @Type(value = GithubAuthenticationInput.class, name = "GITHUB")
})
public abstract class Verifiable {
    private final AuthenticationType type;

    protected Verifiable(AuthenticationType type) {
        this.type = type;
    }

}
