package com.asakaa.synthesis.integration.bedrock;

import org.springframework.stereotype.Component;

@Component
public class BedrockPromptBuilder {

    public String buildDiagnosticPrompt(ClinicalContext context) {
        return String.format("""
                You are an expert clinical decision support system designed to assist healthcare providers in resource-constrained environments.
                
                Your task is to analyze the patient presentation, provide a differential diagnosis with evidence-based reasoning, and ACTIVELY GUIDE the provider on what to do next.
                
                PATIENT INFORMATION:
                %s
                
                CHIEF COMPLAINT:
                %s
                
                VITAL SIGNS:
                %s
                
                LABORATORY RESULTS:
                %s
                
                IMAGING FINDINGS (AI-INTERPRETED):
                %s
                
                AVAILABLE EQUIPMENT:
                %s
                
                LOCAL MEDICATION FORMULARY:
                %s
                
                INSTRUCTIONS:
                1. Generate a ranked differential diagnosis based on the clinical presentation
                2. Adapt all recommendations to the available equipment: %s
                3. Suggest only medications from the local formulary: %s
                4. Prioritize life-threatening conditions and red flags
                5. Consider common conditions in underserved/resource-limited settings
                6. Provide clear clinical reasoning for each differential
                7. Suggest 3-5 HIGH-YIELD CLINICAL QUESTIONS the provider should ask the patient to narrow the diagnosis
                8. Suggest 2-4 PHYSICAL EXAMINATIONS the provider should perform next with their available equipment
                9. Rate the overall urgency: LOW, MODERATE, HIGH, or CRITICAL
                
                Return your response as a valid JSON object with this EXACT structure:
                {
                  "differentials": [
                    {
                      "condition": "condition name",
                      "confidence": 0.85,
                      "reasoning": "detailed clinical reasoning",
                      "recommendedTests": ["test1", "test2"],
                      "redFlags": ["red flag 1", "red flag 2"]
                    }
                  ],
                  "immediateActions": ["action1", "action2"],
                  "safetyNotes": "any critical safety considerations",
                  "nextQuestions": [
                    "Ask: Have you traveled to a malaria-endemic area recently?",
                    "Ask: Do you have any neck stiffness or sensitivity to light?"
                  ],
                  "physicalExams": [
                    "Perform: Check for chest indrawing and count respiratory rate",
                    "Perform: Assess for neck rigidity (meningeal signs)"
                  ],
                  "urgencyLevel": "HIGH"
                }
                
                Ensure confidence scores are between 0.0 and 1.0. Return ONLY the JSON object, no additional text.
                """,
                context.getPatientSummary() != null ? context.getPatientSummary() : "Not provided",
                context.getChiefComplaint() != null ? context.getChiefComplaint() : "Not provided",
                context.getVitals() != null ? context.getVitals() : "Not recorded",
                context.getLabResults() != null ? context.getLabResults() : "None provided",
                context.getImagingFindings() != null ? context.getImagingFindings() : "No previous imaging analysis found for this consultation",
                context.getAvailableEquipment() != null ? context.getAvailableEquipment() : "Standard primary care equipment",
                context.getLocalFormulary() != null ? context.getLocalFormulary() : "WHO Essential Medicines List",
                context.getAvailableEquipment() != null ? context.getAvailableEquipment() : "Standard primary care equipment",
                context.getLocalFormulary() != null ? context.getLocalFormulary() : "WHO Essential Medicines List"
        );
    }


    public String buildTreatmentPrompt(String condition, com.asakaa.synthesis.domain.dto.request.TreatmentRequest request) {
        String medications = request.getAvailableMedications() != null && !request.getAvailableMedications().isEmpty()
                ? String.join(", ", request.getAvailableMedications())
                : "WHO Essential Medicines List";

        return String.format("""
                You are an expert clinical pharmacist and treatment planning specialist for resource-constrained healthcare settings.
                
                Your task is to generate an evidence-based treatment plan for the following confirmed diagnosis.
                
                CONFIRMED DIAGNOSIS:
                %s
                
                PATIENT PARAMETERS:
                - Weight: %s kg
                - Age: %s years
                - Renal Function: %s
                
                AVAILABLE MEDICATIONS:
                %s
                
                INSTRUCTIONS:
                1. Provide first-line and second-line treatment options
                2. Use ONLY medications from the available formulary listed above
                3. Specify exact dosages, frequency, and duration
                4. Adjust dosing for patient weight and age if applicable
                5. Consider renal function in dosing recommendations
                6. Include non-pharmacological interventions where appropriate
                7. Provide clear administration instructions
                8. Include monitoring parameters and follow-up schedule
                9. Add patient education points
                
                Return your response as a valid JSON object with this EXACT structure:
                {
                  "treatments": [
                    {
                      "type": "First-line" or "Second-line" or "Supportive",
                      "drugName": "medication name",
                      "dosage": "dose with frequency (e.g., 500mg twice daily)",
                      "duration": "treatment duration (e.g., 7 days)",
                      "instructions": "detailed administration instructions"
                    }
                  ],
                  "followUpInstructions": "when to follow up and what to monitor",
                  "patientEducation": "key points for patient understanding and compliance"
                }
                
                Return ONLY the JSON object, no additional text.
                """,
                condition,
                request.getPatientWeightKg() != null ? request.getPatientWeightKg().toString() : "Not specified",
                request.getPatientAgeYears() != null ? request.getPatientAgeYears().toString() : "Not specified",
                request.getRenalFunctionNormal() != null && request.getRenalFunctionNormal() ? "Normal" : "Impaired or unknown",
                medications
        );
    }
}
