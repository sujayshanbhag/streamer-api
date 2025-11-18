package com.courage.streamer.api.strategy.auth.otp;

import com.courage.streamer.api.constant.AuthenticationType;
import com.courage.streamer.api.constant.OtpAuthenticationState;
import com.courage.streamer.api.strategy.auth.Verifiable;
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
