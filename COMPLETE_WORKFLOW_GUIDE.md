# Synthesis Platform - Complete End-to-End Workflow

## Overview
This guide provides a complete sequential workflow from clinic registration through to analytics reporting, covering all major features of the Synthesis platform.

**Base URL**: `http://localhost:8080`  
**API Version**: v1  
**Authentication**: JWT Bearer Token

---

## Workflow Summary

0. Clinic Registration (Organization Setup)
1. Provider Registration & Authentication
2. Patient Registration
3. Open Consultation
4. AI Diagnostic Analysis (Text)
5. AI Image Analysis (Optional)
6. Lab Results (Optional)
7. Voice Transcription (Optional)
8. Generate Treatment Plan
9. Close Consultation
10. Specialist Escalation (If Needed)
11. Patient History Review
12. Analytics & Reporting

---

## Prerequisites

### Environment Setup
```bash
# Start PostgreSQL
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=synthesis \
  -e POSTGRES_USER=synthesis_user \
  -e POSTGRES_PASSWORD=password \
  postgres:16

# Start Application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Required Environment Variables
```
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=<your_key>
AWS_SECRET_ACCESS_KEY=<your_secret>
BEDROCK_MODEL_ID=anthropic.claude-3-sonnet-20240229-v1:0
KNOWLEDGE_BASE_ID=<your_kb_id>
JWT_SECRET=<256_bit_secret>
```

---

## Step 0: Clinic Registration (Organization Setup)

**Purpose**: Register a clinic/health facility and create the first admin provider account

**Endpoint**: `POST /api/v1/clinics/register`

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "clinicName": "Nairobi Community Health Center",
  "clinicAddress": "123 Kenyatta Avenue, Nairobi, Kenya",
  "clinicRegion": "East Africa - Kenya",
  "adminName": "Dr. Amara Okonkwo",
  "adminEmail": "amara.okonkwo@nairobi-clinic.org",
  "adminPassword": "SecurePass2026!"
}
```

**Response** (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbWFyYS5va29ua3dvQG5haXJvYmktY2xpbmljLm9yZyIsImlhdCI6MTcwODUxODQwMCwiZXhwIjoxNzA4NjA0ODAwfQ.abc123xyz...",
  "email": "amara.okonkwo@nairobi-clinic.org",
  "name": "Dr. Amara Okonkwo",
  "role": "ADMIN"
}
```

**What Happens**:
1. Clinic entity is created with unique registration code
2. Admin provider account is created and linked to clinic
3. Admin receives JWT token for immediate access
4. Clinic can now onboard additional staff members

**Save**: Copy the `token` value for all subsequent requests

**Additional Clinic Endpoints**:

### Get Clinic Details
```
GET /api/v1/clinics/{clinicId}
Authorization: Bearer <token>
```

**Response**:
```json
{
  "id": 1,
  "name": "Nairobi Community Health Center",
  "address": "123 Kenyatta Avenue, Nairobi, Kenya",
  "region": "East Africa - Kenya",
  "registrationCode": "CLINIC-2026-001",
  "createdAt": "2026-02-19T08:00:00",
  "staff": []
}
```

### Get Clinic Staff
```
GET /api/v1/clinics/{clinicId}/staff
Authorization: Bearer <token>
```

**Response**:
```json
{
  "id": 1,
  "name": "Nairobi Community Health Center",
  "address": "123 Kenyatta Avenue, Nairobi, Kenya",
  "region": "East Africa - Kenya",
  "registrationCode": "CLINIC-2026-001",
  "createdAt": "2026-02-19T08:00:00",
  "staff": [
    {
      "id": 1,
      "name": "Dr. Amara Okonkwo",
      "role": "ADMIN",
      "email": "amara.okonkwo@nairobi-clinic.org"
    },
    {
      "id": 2,
      "name": "Nurse Jane Wanjiru",
      "role": "Nurse",
      "email": "jane.wanjiru@nairobi-clinic.org"
    }
  ]
}
```

### List All Clinics
```
GET /api/v1/clinics
Authorization: Bearer <token>
```

**Response**:
```json
[
  {
    "id": 1,
    "name": "Nairobi Community Health Center",
    "address": "123 Kenyatta Avenue, Nairobi, Kenya",
    "region": "East Africa - Kenya",
    "registrationCode": "CLINIC-2026-001",
    "createdAt": "2026-02-19T08:00:00",
    "staff": []
  }
]
```

---

## Step 1: Provider Registration

**Purpose**: Register a healthcare provider and obtain JWT authentication token

**Endpoint**: `POST /api/v1/auth/register`

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Dr. Amara Okonkwo",
  "role": "General Practitioner",
  "clinicName": "Nairobi Community Health Center",
  "region": "East Africa - Kenya",
  "email": "amara.okonkwo@nairobi-clinic.org",
  "password": "SecurePass2026!"
}
```

**Response** (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbWFyYS5va29ua3dvQG5haXJvYmktY2xpbmljLm9yZyIsImlhdCI6MTcwODUxODQwMCwiZXhwIjoxNzA4NjA0ODAwfQ.abc123xyz...",
  "email": "amara.okonkwo@nairobi-clinic.org",
  "name": "Dr. Amara Okonkwo",
  "role": "General Practitioner"
}
```

**Save**: Copy the `token` value for all subsequent requests

**Alternative - Login** (if already registered):
```
POST /api/v1/auth/login
{
  "email": "amara.okonkwo@nairobi-clinic.org",
  "password": "SecurePass2026!"
}
```

---

## Step 2: Patient Registration

**Purpose**: Register a new patient in the system

**Endpoint**: `POST /api/v1/patients`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbWFyYS5va29ua3dvQG5haXJvYmktY2xpbmljLm9yZyIsImlhdCI6MTcwODUxODQwMCwiZXhwIjoxNzA4NjA0ODAwfQ.abc123xyz...
```

**Request Body**:
```json
{
  "firstName": "Grace",
  "lastName": "Mwangi",
  "dateOfBirth": "1990-07-22",
  "gender": "FEMALE",
  "nationalId": "34567890",
  "bloodGroup": "A_POSITIVE",
  "allergies": "Sulfa drugs, Aspirin",
  "clinicName": "Nairobi Community Health Center"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "firstName": "Grace",
  "lastName": "Mwangi",
  "dateOfBirth": "1990-07-22",
  "gender": "FEMALE",
  "nationalId": "34567890",
  "bloodGroup": "A_POSITIVE",
  "allergies": "Sulfa drugs, Aspirin",
  "clinicName": "Nairobi Community Health Center"
}
```

**Save**: Note the patient `id` (e.g., 1)

**Optional - Search Existing Patient**:
```
GET /api/v1/patients/search?q=Grace
Authorization: Bearer <token>
```

---

## Step 3: Open Consultation

**Purpose**: Start a new clinical consultation session

**Endpoint**: `POST /api/v1/consultations`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <your_token>
```

**Request Body**:
```json
{
  "patientId": 1,
  "chiefComplaint": "Fever, severe headache, and body aches for 4 days. Patient is 32 weeks pregnant.",
  "vitals": "{\"temperature\": 39.5, \"bloodPressure\": \"145/95\", \"heartRate\": 105, \"spO2\": 96, \"weight\": 68}",
  "notes": "Patient is G2P1, 32 weeks pregnant. Reports decreased fetal movement. No history of malaria prophylaxis. Lives in malaria-endemic area."
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Grace Mwangi",
  "providerId": 1,
  "providerName": "Dr. Amara Okonkwo",
  "status": "OPEN",
  "chiefComplaint": "Fever, severe headache, and body aches for 4 days. Patient is 32 weeks pregnant.",
  "vitals": "{\"temperature\": 39.5, \"bloodPressure\": \"145/95\", \"heartRate\": 105, \"spO2\": 96, \"weight\": 68}",
  "notes": "Patient is G2P1, 32 weeks pregnant. Reports decreased fetal movement. No history of malaria prophylaxis. Lives in malaria-endemic area.",
  "openedAt": "2026-02-19T09:15:00",
  "closedAt": null,
  "diagnoses": []
}
```

**Save**: Note the consultation `id` (e.g., 1)

---

## Step 4: AI Diagnostic Analysis (Text-Based)

**Purpose**: Generate AI-powered differential diagnosis with evidence-based guidelines

**Endpoint**: `POST /api/v1/diagnostic/analyze`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <your_token>
```

**Request Body**:
```json
{
  "consultationId": 1,
  "availableEquipment": [
    "Microscope",
    "Rapid diagnostic test (RDT)",
    "Blood pressure monitor",
    "Thermometer",
    "Pulse oximeter"
  ],
  "localFormulary": [
    "Quinine IV",
    "Artesunate IV",
    "Artemether-lumefantrine",
    "Paracetamol",
    "IV fluids",
    "Magnesium sulfate"
  ],
  "additionalNotes": "Rural clinic with limited specialist access. Nearest hospital is 2 hours away."
}
```

**Response** (200 OK):
```json
{
  "consultationId": 1,
  "differentials": [
    {
      "condition": "Severe Malaria in Pregnancy",
      "confidence": 0.88,
      "reasoning": "Based on [Guideline 1] WHO Malaria in Pregnancy Guidelines, patient presents with high fever (39.5°C), severe headache, and body aches in malaria-endemic area without prophylaxis. Pregnancy at 32 weeks increases risk of severe complications. Elevated blood pressure (145/95) and decreased fetal movement are red flags requiring immediate intervention.",
      "recommendedTests": [
        "Blood smear for malaria parasites",
        "Rapid diagnostic test (RDT)",
        "Hemoglobin level",
        "Blood glucose"
      ],
      "redFlags": [
        "High fever >39°C in pregnancy",
        "Elevated blood pressure (possible pre-eclampsia)",
        "Decreased fetal movement",
        "Third trimester pregnancy"
      ]
    },
    {
      "condition": "Pre-eclampsia with Superimposed Infection",
      "confidence": 0.72,
      "reasoning": "Blood pressure of 145/95 at 32 weeks gestation meets criteria for gestational hypertension. Fever and symptoms could indicate superimposed infection. Requires urgent evaluation.",
      "recommendedTests": [
        "Urine protein dipstick",
        "Blood pressure monitoring",
        "Complete blood count"
      ],
      "redFlags": [
        "Elevated blood pressure in pregnancy",
        "Headache (possible severe feature)",
        "Third trimester"
      ]
    }
  ],
  "immediateActions": [
    "Start IV access immediately",
    "Administer IV Artesunate if malaria confirmed",
    "Monitor blood pressure every 30 minutes",
    "Assess fetal heart rate",
    "Prepare for possible emergency referral"
  ],
  "safetyNotes": "This is a high-risk pregnancy emergency. Consider immediate referral to district hospital if condition deteriorates or if severe malaria confirmed.",
  "citations": [
    "WHO_Malaria_in_Pregnancy_Guidelines_2023.pdf: IV Artesunate is the treatment of choice for severe malaria in pregnancy across all trimesters...",
    "WHO_Pre-eclampsia_Guidelines_2023.pdf: Blood pressure ≥140/90 mmHg after 20 weeks gestation indicates gestational hypertension..."
  ],
  "generatedAt": "2026-02-19T09:20:15"
}
```

**Key Information**:
- High-confidence diagnoses (>0.7) are automatically saved to database
- Citations show which WHO guidelines informed the diagnosis
- Red flags highlight life-threatening conditions

---

## Step 5: AI Image Analysis (Optional - Multimodal)

**Purpose**: Analyze medical images (X-rays, ultrasounds, skin lesions) using Claude 3 Sonnet vision

**Endpoint**: `POST /api/v1/diagnostic/analyze-image`

**Headers**:
```
Authorization: Bearer <your_token>
Content-Type: multipart/form-data
```

**Request Body** (Form Data):
```
image: [Select File] - blood_smear.jpg
clinicalContext: Blood smear from 32-week pregnant patient with fever 39.5°C, suspected malaria
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/v1/diagnostic/analyze-image \
  -H "Authorization: Bearer <your_token>" \
  -F "image=@blood_smear.jpg" \
  -F "clinicalContext=Blood smear from 32-week pregnant patient with fever 39.5°C, suspected malaria"
```

**Response** (200 OK):
```json
{
  "description": "The blood smear demonstrates multiple ring-form trophozoites of Plasmodium falciparum within red blood cells. Parasitemia appears to be approximately 5-8%, indicating severe malaria. Some red blood cells show multiple parasites (multiply infected RBCs), which is characteristic of P. falciparum infection. No schizonts are visible in this peripheral smear.",
  "findings": [
    "Multiple ring-form trophozoites present",
    "Plasmodium falciparum identified",
    "Estimated parasitemia: 5-8% (severe)",
    "Multiply infected red blood cells observed",
    "No schizonts in peripheral blood"
  ],
  "analyzedAt": "2026-02-19T09:25:30"
}
```

**Supported Formats**: JPEG, PNG (max 5MB)

**Use Cases**:
- Blood smears (malaria, anemia)
- Chest X-rays (pneumonia, TB)
- Skin lesions (dermatology)
- Ultrasound images (pregnancy complications)

---

## Step 6: Lab Results (Optional)

**Purpose**: Add laboratory test results to consultation for comprehensive diagnosis

**Endpoint**: `POST /api/v1/labs`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <your_token>
```

**Request Body**:
```json
{
  "consultationId": 1,
  "testName": "Complete Blood Count (CBC)",
  "testType": "HEMATOLOGY",
  "result": "{\"hemoglobin\": 9.2, \"wbc\": 12500, \"platelets\": 95000, \"hematocrit\": 28}",
  "unit": "g/dL, cells/μL",
  "referenceRange": "Hb: 12-16 g/dL, WBC: 4000-11000, Platelets: 150000-400000",
  "interpretation": "Low hemoglobin (anemia), elevated WBC (infection), low platelets (thrombocytopenia) - consistent with severe malaria",
  "performedAt": "2026-02-19T09:45:00"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "consultationId": 1,
  "testName": "Complete Blood Count (CBC)",
  "testType": "HEMATOLOGY",
  "result": "{\"hemoglobin\": 9.2, \"wbc\": 12500, \"platelets\": 95000, \"hematocrit\": 28}",
  "unit": "g/dL, cells/μL",
  "referenceRange": "Hb: 12-16 g/dL, WBC: 4000-11000, Platelets: 150000-400000",
  "interpretation": "Low hemoglobin (anemia), elevated WBC (infection), low platelets (thrombocytopenia) - consistent with severe malaria",
  "performedAt": "2026-02-19T09:45:00",
  "createdAt": "2026-02-19T09:50:00"
}
```

**Get Lab Results for Consultation**:
```
GET /api/v1/labs/consultation/1
Authorization: Bearer <your_token>
```

**Response**:
```json
[
  {
    "id": 1,
    "consultationId": 1,
    "testName": "Complete Blood Count (CBC)",
    "testType": "HEMATOLOGY",
    "result": "{\"hemoglobin\": 9.2, \"wbc\": 12500, \"platelets\": 95000, \"hematocrit\": 28}",
    "interpretation": "Low hemoglobin (anemia), elevated WBC (infection), low platelets (thrombocytopenia) - consistent with severe malaria",
    "performedAt": "2026-02-19T09:45:00"
  },
  {
    "id": 2,
    "consultationId": 1,
    "testName": "Blood Glucose",
    "testType": "BIOCHEMISTRY",
    "result": "4.8 mmol/L",
    "interpretation": "Normal fasting glucose",
    "performedAt": "2026-02-19T09:50:00"
  }
]
```

**Common Test Types**:
- `HEMATOLOGY` - Blood counts, coagulation
- `BIOCHEMISTRY` - Glucose, electrolytes, liver/kidney function
- `MICROBIOLOGY` - Cultures, sensitivity tests
- `SEROLOGY` - Antibody tests, rapid diagnostic tests
- `URINALYSIS` - Urine tests
- `IMAGING` - X-ray, ultrasound reports

---

## Step 7: Voice Transcription (Optional)

**Purpose**: Transcribe audio recordings of patient complaints or clinical notes

**Endpoint**: `POST /api/v1/transcribe`

**Headers**:
```
Authorization: Bearer <your_token>
Content-Type: multipart/form-data
```

**Request Body** (Form Data):
```
audio: [Select File] - patient_complaint.mp3
```

**Supported Formats**: WAV, MP3, MP4, M4A

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/v1/transcribe \
  -H "Authorization: Bearer <your_token>" \
  -F "audio=@patient_complaint.mp3"
```

**Response** (200 OK):
```json
{
  "transcription": "Patient reports fever for four days, severe headache, body aches, and chills. She is thirty-two weeks pregnant and has not been taking malaria prophylaxis. She lives in a malaria-endemic area and reports decreased fetal movement since yesterday.",
  "confidence": 0.94,
  "language": "en-US",
  "duration": 45.3,
  "transcribedAt": "2026-02-19T09:18:00"
}
```

**Use Cases**:
- Transcribe patient chief complaints
- Convert voice notes to text for clinical documentation
- Capture consultation notes hands-free
- Support for multiple languages (future)

**Integration with Consultation**:
After transcription, copy the text to consultation notes:
```json
{
  "notes": "Patient reports fever for four days, severe headache, body aches, and chills..."
}
```

---

## Step 8: Generate Treatment Plan

**Purpose**: Generate context-aware, evidence-based treatment plan adapted to local resources

**Endpoint**: `POST /api/v1/diagnostic/treatment`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <your_token>
```

**Request Body**:
```json
{
  "consultationId": 1,
  "confirmedDiagnosis": "Severe Malaria in Pregnancy",
  "patientWeight": 68,
  "localFormulary": [
    "Artesunate IV",
    "Quinine IV",
    "Paracetamol",
    "IV fluids (Normal Saline, Ringer's Lactate)",
    "Folic acid",
    "Iron supplements"
  ],
  "availableResources": "Rural clinic with IV capability, can monitor vitals, nearest hospital 2 hours away",
  "additionalContext": "Patient is 32 weeks pregnant, G2P1, no known drug allergies except sulfa drugs"
}
```

**Response** (200 OK):
```json
{
  "consultationId": 1,
  "diagnosis": "Severe Malaria in Pregnancy",
  "treatments": [
    {
      "drugName": "Artesunate IV",
      "dosage": "2.4 mg/kg (163 mg)",
      "route": "Intravenous",
      "frequency": "At 0, 12, and 24 hours, then once daily",
      "duration": "Minimum 3 days, then switch to oral ACT",
      "instructions": "Administer as slow IV bolus over 2-3 minutes. Dilute in 5% dextrose or normal saline. Monitor for hemolysis post-treatment.",
      "precautions": "Monitor hemoglobin on days 7, 14, and 21 post-treatment for delayed hemolysis. Ensure fetal monitoring during administration."
    },
    {
      "drugName": "Paracetamol",
      "dosage": "1000 mg",
      "route": "Oral",
      "frequency": "Every 6 hours as needed",
      "duration": "Until fever resolves",
      "instructions": "For fever control. Safe in pregnancy. Maximum 4g per day.",
      "precautions": "Monitor liver function if prolonged use."
    },
    {
      "drugName": "IV Fluids (Normal Saline)",
      "dosage": "80 ml/hour",
      "route": "Intravenous",
      "frequency": "Continuous",
      "duration": "Until patient can tolerate oral fluids",
      "instructions": "Maintain hydration. Monitor fluid balance carefully in pregnancy to avoid pulmonary edema.",
      "precautions": "Reduce rate if signs of fluid overload. Monitor urine output."
    },
    {
      "drugName": "Folic Acid",
      "dosage": "5 mg",
      "route": "Oral",
      "frequency": "Once daily",
      "duration": "Continue throughout pregnancy",
      "instructions": "Essential for fetal development and to prevent anemia.",
      "precautions": "None specific."
    }
  ],
  "followUpSchedule": [
    {
      "timepoint": "4 hours",
      "action": "Check vital signs, assess clinical response, verify fetal heart rate"
    },
    {
      "timepoint": "12 hours",
      "action": "Administer second dose of Artesunate, repeat malaria test"
    },
    {
      "timepoint": "24 hours",
      "action": "Administer third dose, assess for oral intake, check parasitemia"
    },
    {
      "timepoint": "Day 3",
      "action": "Switch to oral artemether-lumefantrine if improved, discharge planning"
    },
    {
      "timepoint": "Day 7, 14, 21",
      "action": "Monitor hemoglobin for delayed hemolysis"
    }
  ],
  "patientEducation": [
    "Complete the full course of antimalarial treatment even if feeling better",
    "Use insecticide-treated bed nets to prevent re-infection",
    "Report immediately if fever returns, bleeding, or decreased fetal movement",
    "Attend all antenatal care appointments",
    "Take folic acid and iron supplements daily"
  ],
  "escalationCriteria": [
    "Persistent fever after 48 hours of treatment",
    "Signs of severe malaria complications (seizures, altered consciousness)",
    "Fetal distress or absent fetal movements",
    "Signs of pre-eclampsia progression (severe headache, visual changes)",
    "Inability to tolerate oral medications"
  ],
  "generatedAt": "2026-02-19T09:30:45"
}
```

**Key Features**:
- Dosing calculated by patient weight (68 kg)
- Only medications from local formulary
- Pregnancy-safe recommendations
- Clear follow-up schedule
- Patient education in simple language

---

## Step 9: Update Consultation (Optional)

**Purpose**: Add additional findings, lab results, or clinical notes during consultation

**Endpoint**: `PUT /api/v1/consultations/{id}`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <your_token>
```

**Request Body**:
```json
{
  "chiefComplaint": "Fever, severe headache, and body aches for 4 days. Patient is 32 weeks pregnant. [UPDATED: Malaria confirmed by RDT]",
  "vitals": "{\"temperature\": 38.8, \"bloodPressure\": \"140/92\", \"heartRate\": 98, \"spO2\": 97, \"weight\": 68, \"fetalHeartRate\": 145}",
  "notes": "Patient is G2P1, 32 weeks pregnant. Reports decreased fetal movement. No history of malaria prophylaxis. Lives in malaria-endemic area. [UPDATE 10:00]: RDT positive for P. falciparum. Blood smear confirms 6% parasitemia. Artesunate IV initiated. Fetal heart rate 145 bpm (normal). Patient tolerating treatment well.",
  "status": "IN_PROGRESS"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Grace Mwangi",
  "providerId": 1,
  "providerName": "Dr. Amara Okonkwo",
  "status": "IN_PROGRESS",
  "chiefComplaint": "Fever, severe headache, and body aches for 4 days. Patient is 32 weeks pregnant. [UPDATED: Malaria confirmed by RDT]",
  "vitals": "{\"temperature\": 38.8, \"bloodPressure\": \"140/92\", \"heartRate\": 98, \"spO2\": 97, \"weight\": 68, \"fetalHeartRate\": 145}",
  "notes": "Patient is G2P1, 32 weeks pregnant. Reports decreased fetal movement. No history of malaria prophylaxis. Lives in malaria-endemic area. [UPDATE 10:00]: RDT positive for P. falciparum. Blood smear confirms 6% parasitemia. Artesunate IV initiated. Fetal heart rate 145 bpm (normal). Patient tolerating treatment well.",
  "openedAt": "2026-02-19T09:15:00",
  "closedAt": null,
  "diagnoses": [
    {
      "id": 1,
      "conditionName": "Severe Malaria in Pregnancy",
      "confidence": 0.88,
      "reasoning": "Based on WHO guidelines...",
      "source": "AI_BEDROCK_RAG"
    }
  ]
}
```

---

## Step 10: Close Consultation

**Purpose**: Finalize consultation with outcome and treatment summary

**Endpoint**: `POST /api/v1/consultations/{id}/close`

**Headers**:
```
Authorization: Bearer <your_token>
```

**Request Body**: None (or optional outcome notes in query param)

**Response** (200 OK):
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Grace Mwangi",
  "providerId": 1,
  "providerName": "Dr. Amara Okonkwo",
  "status": "CLOSED",
  "chiefComplaint": "Fever, severe headache, and body aches for 4 days. Patient is 32 weeks pregnant. [UPDATED: Malaria confirmed by RDT]",
  "vitals": "{\"temperature\": 38.8, \"bloodPressure\": \"140/92\", \"heartRate\": 98, \"spO2\": 97, \"weight\": 68, \"fetalHeartRate\": 145}",
  "notes": "Patient is G2P1, 32 weeks pregnant. Reports decreased fetal movement. No history of malaria prophylaxis. Lives in malaria-endemic area. [UPDATE 10:00]: RDT positive for P. falciparum. Blood smear confirms 6% parasitemia. Artesunate IV initiated. Fetal heart rate 145 bpm (normal). Patient tolerating treatment well.",
  "openedAt": "2026-02-19T09:15:00",
  "closedAt": "2026-02-19T11:30:00",
  "diagnoses": [
    {
      "id": 1,
      "conditionName": "Severe Malaria in Pregnancy",
      "confidence": 0.88,
      "reasoning": "Based on WHO Malaria in Pregnancy Guidelines, patient presents with high fever (39.5°C), severe headache, and body aches in malaria-endemic area without prophylaxis...",
      "recommendedTests": ["Blood smear", "RDT", "Hemoglobin", "Blood glucose"],
      "redFlags": ["High fever >39°C in pregnancy", "Elevated blood pressure", "Decreased fetal movement"],
      "source": "AI_BEDROCK_RAG"
    }
  ]
}
```

**Note**: Once closed, consultation cannot be reopened. All data is immutable for audit purposes.

---

## Step 11: Specialist Escalation (If Needed)

**Purpose**: Escalate complex cases to specialists with AI-generated case summary

**Endpoint**: `POST /api/v1/escalation/refer`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <your_token>
```

**Request Body**:
```json
{
  "consultationId": 1,
  "urgencyLevel": "URGENT",
  "specialistType": "Obstetrician",
  "reason": "Severe malaria in pregnancy at 32 weeks with elevated blood pressure. Requires specialist obstetric care and possible early delivery assessment.",
  "preferredContactMethod": "PHONE",
  "additionalNotes": "Patient is 2 hours from district hospital. Artesunate treatment initiated. Fetal heart rate currently stable at 145 bpm."
}
```

**Urgency Levels**:
- `ROUTINE` - Response within 2-3 days
- `URGENT` - Response within 24 hours
- `EMERGENCY` - Immediate response required

**Response** (201 Created):
```json
{
  "id": 1,
  "referralId": "REF-2026-001234",
  "consultationId": 1,
  "patientName": "Grace Mwangi",
  "urgencyLevel": "URGENT",
  "specialistType": "Obstetrician",
  "status": "PENDING",
  "caseSummary": {
    "patientDemographics": {
      "name": "Grace Mwangi",
      "age": 35,
      "gender": "FEMALE",
      "nationalId": "34567890"
    },
    "clinicalPresentation": {
      "chiefComplaint": "Fever, severe headache, and body aches for 4 days. Patient is 32 weeks pregnant.",
      "vitals": {
        "temperature": 38.8,
        "bloodPressure": "140/92",
        "heartRate": 98,
        "spO2": 97,
        "fetalHeartRate": 145
      },
      "duration": "4 days"
    },
    "diagnosis": {
      "primary": "Severe Malaria in Pregnancy",
      "confidence": 0.88,
      "differentials": ["Pre-eclampsia with superimposed infection"]
    },
    "investigations": {
      "completed": ["RDT positive for P. falciparum", "Blood smear: 6% parasitemia"],
      "pending": ["Hemoglobin", "Urine protein"]
    },
    "treatment": {
      "initiated": ["Artesunate IV 163mg at 0, 12, 24h", "IV fluids", "Paracetamol"],
      "response": "Patient tolerating treatment, fever decreasing"
    },
    "redFlags": [
      "Severe malaria in third trimester pregnancy",
      "Elevated blood pressure (140/92)",
      "History of decreased fetal movement",
      "High parasitemia (6%)"
    ],
    "clinicCapabilities": "Rural clinic, 2 hours from hospital, IV capability, basic monitoring",
    "reasonForReferral": "Severe malaria in pregnancy at 32 weeks with elevated blood pressure. Requires specialist obstetric care and possible early delivery assessment."
  },
  "createdAt": "2026-02-19T11:35:00",
  "expectedResponseTime": "2026-02-20T11:35:00",
  "notificationSent": true
}
```

**What Happens Next**:
1. Specialist receives notification (email/SMS)
2. Complete case summary is available for review
3. Specialist can respond with recommendations
4. Provider receives notification when specialist responds
5. Telemedicine consultation can be scheduled if needed

---

## Step 12: Review Patient History

**Purpose**: View complete consultation history for longitudinal care

**Endpoint**: `GET /api/v1/consultations/patient/{patientId}/history`

**Headers**:
```
Authorization: Bearer <your_token>
```

**Request**: `GET /api/v1/consultations/patient/1/history`

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "patientId": 1,
    "patientName": "Grace Mwangi",
    "providerId": 1,
    "providerName": "Dr. Amara Okonkwo",
    "status": "CLOSED",
    "chiefComplaint": "Fever, severe headache, and body aches for 4 days. Patient is 32 weeks pregnant.",
    "vitals": "{\"temperature\": 38.8, \"bloodPressure\": \"140/92\", \"heartRate\": 98, \"spO2\": 97}",
    "notes": "Severe malaria in pregnancy confirmed. Artesunate IV initiated.",
    "openedAt": "2026-02-19T09:15:00",
    "closedAt": "2026-02-19T11:30:00",
    "diagnoses": [
      {
        "id": 1,
        "conditionName": "Severe Malaria in Pregnancy",
        "confidence": 0.88,
        "source": "AI_BEDROCK_RAG"
      }
    ]
  }
]
```

**Use Cases**:
- Review patient's medical history before new consultation
- Track treatment outcomes over time
- Identify patterns or recurring conditions
- Prepare case summaries for referrals

---

## Step 13: Analytics & Reporting

### 11a. Disease Trends

**Purpose**: Monitor disease patterns and detect outbreaks

**Endpoint**: `GET /api/v1/analytics/trends`

**Headers**:
```
Authorization: Bearer <your_token>
```

**Query Parameters**:
```
region=East Africa - Kenya
from=2026-01-01
to=2026-02-19
```

**Request**: `GET /api/v1/analytics/trends?region=East%20Africa%20-%20Kenya&from=2026-01-01&to=2026-02-19`

**Response** (200 OK):
```json
[
  {
    "condition": "Malaria",
    "caseCount": 45,
    "period": "2026-02",
    "region": "East Africa - Kenya",
    "trend": "INCREASING",
    "percentageChange": 28.5
  },
  {
    "condition": "Pneumonia",
    "caseCount": 23,
    "period": "2026-02",
    "region": "East Africa - Kenya",
    "trend": "STABLE",
    "percentageChange": 2.1
  },
  {
    "condition": "Tuberculosis",
    "caseCount": 12,
    "period": "2026-02",
    "region": "East Africa - Kenya",
    "trend": "STABLE",
    "percentageChange": -5.3
  }
]
```

**Insights**:
- 28.5% increase in malaria cases (possible outbreak)
- Pneumonia cases stable
- TB cases slightly decreasing

---

### 11b. Treatment Outcomes

**Purpose**: Track treatment success rates and quality metrics

**Endpoint**: `GET /api/v1/analytics/outcomes`

**Headers**:
```
Authorization: Bearer <your_token>
```

**Request**: `GET /api/v1/analytics/outcomes`

**Response** (200 OK):
```json
[
  {
    "condition": "Malaria",
    "totalCases": 45,
    "resolved": 38,
    "escalated": 5,
    "ongoing": 2,
    "successRate": 84.4,
    "averageConfidence": 0.82
  },
  {
    "condition": "Pneumonia",
    "totalCases": 23,
    "resolved": 20,
    "escalated": 2,
    "ongoing": 1,
    "successRate": 87.0,
    "averageConfidence": 0.79
  }
]
```

**Key Metrics**:
- Success Rate: Percentage of cases resolved without escalation
- Average Confidence: Mean AI diagnostic confidence
- Escalation Rate: Percentage requiring specialist referral

---

### 11c. Clinic Summary Dashboard

**Purpose**: Overall clinic performance and utilization metrics

**Endpoint**: `GET /api/v1/analytics/clinic/{clinicName}`

**Headers**:
```
Authorization: Bearer <your_token>
```

**Request**: `GET /api/v1/analytics/clinic/Nairobi%20Community%20Health%20Center`

**Response** (200 OK):
```json
{
  "clinicName": "Nairobi Community Health Center",
  "period": "2026-02",
  "totalPatients": 156,
  "totalConsultations": 203,
  "activeConsultations": 8,
  "closedConsultations": 195,
  "topConditions": [
    {
      "condition": "Malaria",
      "count": 45,
      "percentage": 22.2
    },
    {
      "condition": "Pneumonia",
      "count": 23,
      "percentage": 11.3
    },
    {
      "condition": "Hypertension",
      "count": 18,
      "percentage": 8.9
    }
  ],
  "providerUtilization": [
    {
      "providerName": "Dr. Amara Okonkwo",
      "consultations": 87,
      "averageConsultationTime": "18 minutes"
    },
    {
      "providerName": "Nurse Jane Wanjiru",
      "consultations": 116,
      "averageConsultationTime": "15 minutes"
    }
  ],
  "escalationRate": 6.4,
  "averageDiagnosticConfidence": 0.81
}
```

**Dashboard Insights**:
- 203 consultations in February
- Malaria is top condition (22.2%)
- 6.4% escalation rate (healthy)
- Average AI confidence: 81%

---

## Complete Workflow Summary

### Sequential Flow Diagram

```
0. CLINIC REGISTRATION (Organization Setup)
   └─> POST /api/v1/clinics/register
       └─> Receive JWT Token + Clinic ID
           │
1. PROVIDER REGISTRATION (Additional Staff)
   └─> POST /api/v1/auth/register
       └─> Receive JWT Token
           │
2. PATIENT REGISTRATION
   └─> POST /api/v1/patients
       └─> Receive Patient ID
           │
3. OPEN CONSULTATION
   └─> POST /api/v1/consultations
       └─> Receive Consultation ID
           │
4. AI DIAGNOSTIC ANALYSIS
   └─> POST /api/v1/diagnostic/analyze
       └─> Receive Differential Diagnoses + Citations
           │
5. IMAGE ANALYSIS (Optional)
   └─> POST /api/v1/diagnostic/analyze-image
       └─> Receive Image Findings
           │
6. LAB RESULTS (Optional)
   └─> POST /api/v1/labs
       └─> Add Laboratory Test Results
           │
7. VOICE TRANSCRIPTION (Optional)
   └─> POST /api/v1/transcribe
       └─> Convert Audio to Text
           │
8. GENERATE TREATMENT PLAN
   └─> POST /api/v1/diagnostic/treatment
       └─> Receive Context-Aware Treatment
           │
9. UPDATE CONSULTATION (Optional)
   └─> PUT /api/v1/consultations/{id}
       └─> Add Lab Results / Clinical Notes
           │
10. CLOSE CONSULTATION
    └─> POST /api/v1/consultations/{id}/close
        └─> Finalize Record
            │
11. ESCALATE TO SPECIALIST (If Needed)
    └─> POST /api/v1/escalation/refer
        └─> Generate Case Summary + Notify Specialist
            │
12. REVIEW PATIENT HISTORY
    └─> GET /api/v1/consultations/patient/{id}/history
        └─> View Complete Medical History
            │
13. ANALYTICS & REPORTING
    ├─> GET /api/v1/analytics/trends
    ├─> GET /api/v1/analytics/outcomes
    └─> GET /api/v1/analytics/clinic/{name}
        └─> Monitor Population Health
```

---

## Additional Endpoints Reference

### Clinic Management
- `POST /api/v1/clinics/register` - Register clinic with admin
- `GET /api/v1/clinics/{clinicId}` - Get clinic details
- `GET /api/v1/clinics/{clinicId}/staff` - Get clinic staff list
- `GET /api/v1/clinics` - List all clinics

### Authentication
- `POST /api/v1/auth/register` - Register provider
- `POST /api/v1/auth/login` - Login existing provider

### Patient Management
- `POST /api/v1/patients` - Create patient
- `GET /api/v1/patients/{id}` - Get patient details
- `PUT /api/v1/patients/{id}` - Update patient
- `GET /api/v1/patients` - List all patients (paginated)
- `GET /api/v1/patients/search?q={query}` - Search patients

### Consultations
- `POST /api/v1/consultations` - Open consultation
- `PUT /api/v1/consultations/{id}` - Update consultation
- `POST /api/v1/consultations/{id}/close` - Close consultation
- `GET /api/v1/consultations/{id}` - Get consultation details
- `GET /api/v1/consultations/{id}/history` - Get consultation timeline
- `GET /api/v1/consultations/provider/active` - Get provider's active consultations
- `GET /api/v1/consultations/patient/{patientId}/history` - Get patient history

### AI Diagnostics
- `POST /api/v1/diagnostic/analyze` - Text-based diagnosis
- `POST /api/v1/diagnostic/analyze-image` - Image analysis (multimodal)
- `POST /api/v1/diagnostic/treatment` - Generate treatment plan
- `GET /api/v1/diagnostic/treatment/{diagnosisId}` - Get treatments by diagnosis

### Laboratory
- `POST /api/v1/labs` - Add lab result
- `GET /api/v1/labs/consultation/{consultationId}` - Get lab results for consultation

### Voice Transcription
- `POST /api/v1/transcribe` - Transcribe audio to text

### Escalation
- `POST /api/v1/escalation/refer` - Escalate to specialist

### Analytics
- `GET /api/v1/analytics/trends` - Disease trends
- `GET /api/v1/analytics/outcomes` - Treatment outcomes
- `GET /api/v1/analytics/clinic/{name}` - Clinic dashboard

**Total Endpoints**: 32 (29 fully implemented, 3 enhanced stubs)

---

## Common HTTP Status Codes

| Code | Status | Meaning |
|------|--------|---------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request body or parameters |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource (e.g., duplicate national ID) |
| 500 | Internal Server Error | Server error |

---

## Error Response Format

All errors return a consistent JSON structure:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Patient with national ID 34567890 already exists",
  "httpStatus": 400,
  "timestamp": "2026-02-19T09:15:00"
}
```

**Error Codes**:
- `VALIDATION_ERROR` - Invalid input data
- `RESOURCE_NOT_FOUND` - Entity not found
- `DIAGNOSTIC_ERROR` - AI analysis failed
- `AUTHENTICATION_ERROR` - Auth failure
- `INTERNAL_ERROR` - Server error

---

## Best Practices

### 1. Token Management
- Store JWT token securely (not in localStorage for production)
- Token expires after 24 hours - implement refresh logic
- Include token in all authenticated requests

### 2. Error Handling
- Always check HTTP status codes
- Parse error responses for user-friendly messages
- Implement retry logic for 5xx errors
- Log errors for debugging

### 3. Data Validation
- Validate input on client side before sending
- Handle validation errors gracefully
- Provide clear error messages to users

### 4. Performance
- Use pagination for list endpoints
- Cache frequently accessed data (patient lists, formularies)
- Implement request timeouts (30s for AI analysis)

### 5. Security
- Always use HTTPS in production
- Never log sensitive data (passwords, tokens)
- Implement rate limiting on client side
- Validate all user inputs

---

## Testing Checklist

### Smoke Test (5 minutes)
- [ ] Register provider and receive token
- [ ] Create patient
- [ ] Open consultation
- [ ] Run diagnostic analysis
- [ ] Close consultation

### Full Integration Test (15 minutes)
- [ ] Complete workflow from registration to analytics
- [ ] Test image analysis with sample image
- [ ] Generate treatment plan
- [ ] Escalate case to specialist
- [ ] Review patient history
- [ ] Check analytics dashboards

### Edge Cases
- [ ] Duplicate patient registration (should fail)
- [ ] Invalid JWT token (should return 401)
- [ ] Non-existent patient ID (should return 404)
- [ ] Expired token (should return 401)
- [ ] Large image upload >5MB (should fail)
- [ ] Invalid image format (should fail)

---

## Troubleshooting

### Issue: 401 Unauthorized
**Cause**: Missing or expired JWT token  
**Solution**: Re-login or register to get new token

### Issue: 404 Not Found
**Cause**: Invalid resource ID  
**Solution**: Verify ID exists using GET endpoint first

### Issue: Diagnostic analysis takes >30s
**Cause**: AWS Bedrock latency or Knowledge Base query slow  
**Solution**: Check AWS region, verify KB is indexed, implement timeout

### Issue: Image analysis fails
**Cause**: Invalid format, file too large, or corrupted  
**Solution**: Verify JPEG/PNG format, compress to <5MB, test with different image

### Issue: Empty analytics data
**Cause**: No consultations in date range  
**Solution**: Verify date range, check if consultations exist

---

## Production Deployment Checklist

### Infrastructure
- [ ] PostgreSQL database configured (RDS Multi-AZ)
- [ ] AWS Bedrock access configured
- [ ] Knowledge Base created and indexed
- [ ] S3 bucket for images configured
- [ ] Load balancer with SSL certificate
- [ ] CloudWatch logging enabled

### Security
- [ ] JWT secret is 256-bit random string
- [ ] Database credentials in AWS Secrets Manager
- [ ] CORS configured for production domain only
- [ ] Rate limiting enabled
- [ ] HTTPS enforced

### Monitoring
- [ ] CloudWatch alarms configured
- [ ] Error tracking enabled
- [ ] Performance monitoring active
- [ ] Database backup schedule set

### Documentation
- [ ] API documentation published
- [ ] User training materials ready
- [ ] Support contact information available
- [ ] Incident response plan documented

---

## Support & Resources

### Documentation
- [Synthesis Hackathon Blueprint](Synthesis_Hackathon_Blueprint.md)
- [Frontend Product Brief](SYNTHESIS_FRONTEND_PRODUCT_BRIEF.md)
- [Multimodal Diagnostics Guide](MULTIMODAL_DIAGNOSTICS.md)
- [RAG Knowledge Base Guide](RAG_KNOWLEDGE_BASE.md)
- [Postman Patient History API](POSTMAN_PATIENT_HISTORY_API.md)

### External Resources
- [AWS Bedrock Documentation](https://docs.aws.amazon.com/bedrock/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

### Contact
- **Technical Support**: dev@synthesis.health
- **Clinical Questions**: clinical@synthesis.health
- **General Inquiries**: info@synthesis.health

---

**Document Version**: 1.0  
**Last Updated**: February 19, 2026  
**Author**: Synthesis Development Team
