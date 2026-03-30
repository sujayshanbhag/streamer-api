package com.courage.streamer.api.strategy.auth.github;

import com.courage.streamer.api.constant.AuthenticationType;
import com.courage.streamer.api.strategy.auth.Verifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubAuthenticationInput  extends Verifiable {
    private String authorizationCode;

    public GithubAuthenticationInput() {
        super(AuthenticationType.GITHUB);
    }
    @JsonCreator
    public GithubAuthenticationInput(String authorizationCode) {
        super(AuthenticationType.GITHUB);
        this.authorizationCode = authorizationCode;
    }
}
