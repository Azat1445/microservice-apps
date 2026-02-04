package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.request.LoginRequestDto;
import org.example.orderservice.dto.request.RefreshTokenRequest;
import org.example.orderservice.dto.request.RegisterRequestDto;
import org.example.orderservice.dto.response.AuthResponseDto;
import org.example.orderservice.entity.User;
import org.example.orderservice.exception.InvalidCredentialsException;
import org.example.orderservice.exception.UserAlreadyExistsException;
import org.example.orderservice.mapper.UserMapperDto;
import org.example.orderservice.repository.UserRepository;
import org.example.orderservice.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapperDto userMapper;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        log.info("attempting to register new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.info("Registration failed: username {} already exists", request.getUsername());
            throw new UserAlreadyExistsException("Username '" + request.getUsername() + "' already token");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.info("Registration failed: email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("Email '" + request.getEmail() + "' already token");
        }

        User user = userMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        User savedUser = userRepository.save(user);
        log.info("User {} registered successfully with idL {}", savedUser.getUsername(), savedUser.getId());

        String accessToken = jwtUtil.generateAccessToken(savedUser.getUsername(), savedUser.getId(), savedUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUsername());

        log.info("Jwt tokens generated for user: {}", savedUser.getUsername());

        return new AuthResponseDto(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            log.info("User {} authenticated successfully", request.getUsername());
        } catch (BadCredentialsException e) {
            log.error("Login failed for user {}: invalid credentials", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        log.info("Jwt tokens generated for user: {}", user.getUsername());

        return new AuthResponseDto(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token request received");

        try {
            String username = jwtUtil.extractUsername(request.getRefreshToken());

            if (!jwtUtil.validateToken(request.getRefreshToken(), username)) {
                log.info("Invalid refresh token for user: {}", username);
                throw new InvalidCredentialsException("Invalid refresh token");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new InvalidCredentialsException("User not found"));

            String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId(), user.getRole());

            log.info("Access token generated for user: {}", username);

            return new AuthResponseDto(newAccessToken, request.getRefreshToken());
        } catch (Exception e) {
            log.error("Refresh token validation failed", e.getMessage());
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }


    }
}
