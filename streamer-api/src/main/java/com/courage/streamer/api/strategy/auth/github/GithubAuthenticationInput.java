package com.courage.streamer.api.strategy.auth.github;

import com.courage.streamer.api.constant.AuthenticationType;
import com.courage.streamer.api.strategy.auth.Verifiable;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubAuthenticationInput  extends Verifiable {
    private String token;

    public GithubAuthenticationInput() {
        super(AuthenticationType.GITHUB);
    }
    @JsonCreator
    public GithubAuthenticationInput(String token) {
        super(AuthenticationType.GITHUB);
        this.token = token;
    }
}
