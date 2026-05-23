package com.medicore.hms.service;

import com.medicore.hms.dto.request.LoginRequest;
import com.medicore.hms.dto.request.RefreshTokenRequest;
import com.medicore.hms.dto.request.RegisterRequest;
import com.medicore.hms.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String accessToken, String email);
}
