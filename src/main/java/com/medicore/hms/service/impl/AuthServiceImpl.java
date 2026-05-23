package com.medicore.hms.service.impl;

import com.medicore.hms.dto.request.LoginRequest;
import com.medicore.hms.dto.request.RefreshTokenRequest;
import com.medicore.hms.dto.request.RegisterRequest;
import com.medicore.hms.dto.response.AuthResponse;
import com.medicore.hms.dto.response.DoctorResponse;
import com.medicore.hms.entity.Doctor;
import com.medicore.hms.exception.BusinessException;
import com.medicore.hms.exception.DuplicateResourceException;
import com.medicore.hms.exception.InvalidTokenException;
import com.medicore.hms.repository.DoctorRepository;
import com.medicore.hms.security.service.JwtService;
import com.medicore.hms.service.AuthService;
import com.medicore.hms.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final DoctorRepository     doctorRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService    userDetailsService;
    private final AuditService          auditService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Uniqueness checks
        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("License number already registered: " + request.getLicenseNumber());
        }

        Doctor doctor = Doctor.builder()
            .fullName(request.getFullName())
            .email(request.getEmail().toLowerCase().trim())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .licenseNumber(request.getLicenseNumber())
            .specialization(request.getSpecialization())
            .department(request.getDepartment())
            .phone(request.getPhone())
            .qualification(request.getQualification())
            .experienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0)
            .isActive(true)
            .isAvailable(true)
            .build();

        Doctor saved = doctorRepository.save(doctor);
        log.info("New doctor registered: {} [{}]", saved.getEmail(), saved.getRole());

        auditService.log("Doctor", saved.getId(), "REGISTER",
            null, null, "New doctor registered: " + saved.getFullName());

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.of(
            accessToken,
            refreshToken,
            jwtService.getAccessTokenExpirySeconds(),
            toResponse(saved)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Spring Security handles bad credentials → throws BadCredentialsException
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase().trim(),
                request.getPassword()
            )
        );

        Doctor doctor = doctorRepository.findByEmail(request.getEmail().toLowerCase().trim())
            .orElseThrow(() -> new BusinessException("Doctor not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(doctor.getEmail());
        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Doctor logged in: {}", doctor.getEmail());

        return AuthResponse.of(
            accessToken,
            refreshToken,
            jwtService.getAccessTokenExpirySeconds(),
            toResponse(doctor)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String email = jwtService.extractUsername(refreshToken);

        if (email == null) {
            throw new InvalidTokenException("Cannot extract user from refresh token");
        }

        if (!jwtService.isRefreshTokenValid(refreshToken, email)) {
            throw new InvalidTokenException("Refresh token is invalid or expired. Please login again.");
        }

        Doctor doctor = doctorRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidTokenException("Doctor associated with token not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Rotate: invalidate old refresh, issue new pair
        jwtService.invalidateRefreshToken(email);
        String newAccessToken  = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.of(
            newAccessToken,
            newRefreshToken,
            jwtService.getAccessTokenExpirySeconds(),
            toResponse(doctor)
        );
    }

    @Override
    public void logout(String accessToken, String email) {
        jwtService.blacklistToken(accessToken);
        jwtService.invalidateRefreshToken(email);
        log.info("Doctor logged out: {}", email);
    }

    // ── Mapper helper ────────────────────────────────────────────────────────

    private DoctorResponse toResponse(Doctor doctor) {
        return DoctorResponse.builder()
            .id(doctor.getId())
            .fullName(doctor.getFullName())
            .email(doctor.getEmail())
            .role(doctor.getRole())
            .specialization(doctor.getSpecialization())
            .department(doctor.getDepartment())
            .licenseNumber(doctor.getLicenseNumber())
            .phone(doctor.getPhone())
            .qualification(doctor.getQualification())
            .experienceYears(doctor.getExperienceYears())
            .isActive(doctor.getIsActive())
            .isAvailable(doctor.getIsAvailable())
            .profileImageUrl(doctor.getProfileImageUrl())
            .createdAt(doctor.getCreatedAt())
            .build();
    }
}
