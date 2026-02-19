package com.asakaa.synthesis.integration.bedrock;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClinicalContext {

    private String patientSummary;
    private String chiefComplaint;
    private String vitals;
    private String availableEquipment;
    private String localFormulary;
    private String labResults;
    private String imagingFindings;
}
