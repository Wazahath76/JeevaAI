package com.medicore.hms.util;

import com.medicore.hms.entity.Doctor;
import com.medicore.hms.exception.ResourceNotFoundException;
import com.medicore.hms.exception.UnauthorizedActionException;
import com.medicore.hms.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final DoctorRepository doctorRepository;

    /**
     * Returns the authenticated Doctor entity from the database.
     * Throws UnauthorizedActionException if no authentication present.
     */
    public Doctor getCurrentDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedActionException("Not authenticated");
        }
        String email = auth.getName();
        return doctorRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "email", email));
    }

    /**
     * Returns the email of the currently authenticated user.
     */
    public String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedActionException("Not authenticated");
        }
        return auth.getName();
    }

    /**
     * Checks whether the current user has the given role.
     */
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
