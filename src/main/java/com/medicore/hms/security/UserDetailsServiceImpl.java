package com.medicore.hms.security;

import com.medicore.hms.entity.Doctor;
import com.medicore.hms.exception.ResourceNotFoundException;
import com.medicore.hms.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Doctor doctor = doctorRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Doctor not found: " + email));

        if (!doctor.getIsActive()) {
            throw new UsernameNotFoundException("Doctor account is deactivated: " + email);
        }

        return User.builder()
            .username(doctor.getEmail())
            .password(doctor.getPasswordHash())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + doctor.getRole().name())))
            .accountExpired(false)
            .accountLocked(!doctor.getIsActive())
            .credentialsExpired(false)
            .disabled(!doctor.getIsActive())
            .build();
    }
}
