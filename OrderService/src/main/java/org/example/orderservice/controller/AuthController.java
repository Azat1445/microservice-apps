package org.example.orderservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.request.LoginRequestDto;
import org.example.orderservice.dto.request.RefreshTokenRequest;
import org.example.orderservice.dto.request.RegisterRequestDto;
import org.example.orderservice.dto.response.AuthResponseDto;
import org.example.orderservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        log.info("Registration request received for username: {}", request.getUsername());

        AuthResponseDto response = authService.register(request);

        log.info("User {} registered successfully", request.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Login request received for username: {}", request.getUsername());

        AuthResponseDto response = authService.login(request);

        log.info("User {} logged successfully", request.getUsername());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");

        AuthResponseDto response = authService.refreshToken(request);

        log.info("Access token refreshed successfully");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
