package com.medicore.hms.service;

import com.medicore.hms.dto.request.CreateSurgicalPlanRequest;
import com.medicore.hms.dto.request.UpdateSurgicalPlanRequest;
import com.medicore.hms.dto.response.SurgicalPlanResponse;

import java.util.List;
import java.util.UUID;

public interface SurgicalPlanService {
    SurgicalPlanResponse     create(CreateSurgicalPlanRequest request);
    SurgicalPlanResponse     update(UUID planId, UpdateSurgicalPlanRequest request);
    SurgicalPlanResponse     getById(UUID planId);
    List<SurgicalPlanResponse> getForPatient(UUID patientId);
    List<SurgicalPlanResponse> getMySurgeries();   // surgeon's view
    List<SurgicalPlanResponse> getMyAnaesthesia(); // anaesthetist's view
}
