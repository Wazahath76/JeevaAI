package com.medicore.hms.dto.request;

import com.medicore.hms.enums.BloodGroup;
import com.medicore.hms.enums.PatientStatus;
import lombok.Data;

@Data
public class UpdatePatientRequest {
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private BloodGroup bloodGroup;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String knownAllergies;
    private String chronicConditions;
    private String familyHistory;
    private String pastSurgeries;
    private PatientStatus status;
    private String ward;
    private String bedNumber;
    private String insuranceProvider;
    private String insurancePolicyNo;
    private String notes;
}
