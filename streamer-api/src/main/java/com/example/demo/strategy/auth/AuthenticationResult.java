package com.example.demo.strategy.auth;

import com.example.demo.constant.AuthenticationStatus;
import lombok.Getter;

@Getter
public class AuthenticationResult {
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String pictureUrl;
    private final AuthenticationStatus status;
    private final String message;

    private AuthenticationResult(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.pictureUrl = builder.pictureUrl;
        this.status = builder.status;
        this.message = builder.message;
    }

    public static AuthenticationResult failed(String message) {
        return new Builder()
                .status(AuthenticationStatus.FAILED)
                .message(message)
                .build();
    }

    public static class Builder {
        private String name;
        private String email;
        private String phoneNumber;
        private String pictureUrl;
        private AuthenticationStatus status;
        private String message;

        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }
        public Builder pictureUrl(String pictureUrl) {
            this.pictureUrl = pictureUrl;
            return this;
        }

        public Builder status(AuthenticationStatus status) {
            this.status = status;
            return this;
        }

        private Builder message(String message) {
            this.message = message;
            return this;
        }

        public AuthenticationResult build() {
            if (this.email == null || this.email.isEmpty()) {
                return failed("Email not found");
            }
            return new AuthenticationResult(this);
        }
    }
}
