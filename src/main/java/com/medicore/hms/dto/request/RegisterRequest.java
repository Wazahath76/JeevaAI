package com.medicore.hms.dto.request;

import com.medicore.hms.enums.DoctorRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 255, message = "Name must be 2–255 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain uppercase, lowercase, digit and special character"
    )
    private String password;

    @NotNull(message = "Role is required")
    private DoctorRole role;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private String specialization;
    private String department;
    private String phone;
    private String qualification;
    private Integer experienceYears;
}
