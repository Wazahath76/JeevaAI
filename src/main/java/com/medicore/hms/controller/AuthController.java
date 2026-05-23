package com.medicore.hms.controller;

import com.medicore.hms.dto.request.LoginRequest;
import com.medicore.hms.dto.request.RefreshTokenRequest;
import com.medicore.hms.dto.request.RegisterRequest;
import com.medicore.hms.dto.response.ApiResponse;
import com.medicore.hms.dto.response.AuthResponse;
import com.medicore.hms.service.AuthService;
import com.medicore.hms.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService  authService;
    private final SecurityUtil securityUtil;

    /**
     * POST /api/auth/register
     * Registers a new doctor account and returns tokens immediately.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Registration successful", response));
    }

    /**
     * POST /api/auth/login
     * Authenticates doctor credentials and returns access + refresh tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * POST /api/auth/refresh
     * Issues a new access token using a valid refresh token (rotation strategy).
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    /**
     * POST /api/auth/logout
     * Blacklists the current access token and invalidates the refresh token in Redis.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            String email       = securityUtil.getCurrentEmail();
            authService.logout(accessToken, email);
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * GET /api/auth/me
     * Returns the authenticated doctor's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Object>> me() {
        var doctor = securityUtil.getCurrentDoctor();
        return ResponseEntity.ok(ApiResponse.success(doctor));
    }
}
