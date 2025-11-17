package com.example.demo.strategy.auth.otp;

import com.example.demo.constant.AuthenticationType;
import com.example.demo.constant.OtpAuthenticationState;
import com.example.demo.strategy.auth.Verifiable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpAuthenticationInput extends Verifiable {
    private String email;
    private String otpCode;
    private OtpAuthenticationState state;

    // TODO: Review and update the input structure when OTP strategy is implemented
    public OtpAuthenticationInput() {
        super(AuthenticationType.OTP);
    }

}
