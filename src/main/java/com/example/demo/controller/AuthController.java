package com.example.demo.controller;

import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.dto.auth.RegistrationRequest;
import com.example.demo.model.user.UserEntity;
import com.example.demo.security.JwtService;
import com.example.demo.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        UserEntity user = userService.authenticateAndGetDetails(
                loginRequest.username(),
                loginRequest.password()
        );
        String token = jwtService.generateToken(user);
        return new LoginResponse(token);
    }


    @PostMapping("register")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        UserEntity user = userService.registerNewUser(registrationRequest);
        return ResponseEntity.ok().body(user.getId());
    }

}
