package com.example.demo.strategy.auth.google;

import com.example.demo.constant.AuthenticationType;
import com.example.demo.strategy.auth.Verifiable;
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
