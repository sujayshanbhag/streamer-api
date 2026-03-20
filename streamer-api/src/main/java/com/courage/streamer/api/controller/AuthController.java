package com.courage.streamer.api.controller;


import com.courage.streamer.api.model.dto.RefreshTokenDto;
import com.courage.streamer.api.model.dto.TokenResponseDto;
import com.courage.streamer.api.service.impl.AuthenticationServiceImpl;
import com.courage.streamer.api.strategy.auth.Verifiable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationServiceImpl authService;

    public AuthController(AuthenticationServiceImpl authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public TokenResponseDto register(@RequestBody Verifiable input) {
       return authService.register(input);
    }

    @PostMapping("/login")
    public TokenResponseDto login(@RequestBody Verifiable input) {
        return authService.login(input);
    }

    @PostMapping("/refresh")
    public TokenResponseDto refresh(@RequestBody RefreshTokenDto refreshToken) {
        return authService.refresh(refreshToken.getRefreshToken());
    }


}
