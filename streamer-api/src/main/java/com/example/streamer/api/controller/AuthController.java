package com.example.streamer.api.controller;


import com.example.streamer.api.repository.UserRepository;
import com.example.streamer.api.service.impl.AuthenticationServiceImpl;
import com.example.streamer.api.strategy.auth.Verifiable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationServiceImpl authService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationServiceImpl authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public String register(@RequestBody Verifiable input) {
       return authService.register(input);
    }

    @PostMapping("/login")
    public String login(@RequestBody Verifiable input) {
        return authService.login(input);
    }

    @PostMapping("/test")
    public String test(@RequestBody Verifiable input) {
        userRepository.findAll().forEach(user -> {
            user.setIsActive(false);
            userRepository.save(user);
        });
        return "Test completed";
    }

}
