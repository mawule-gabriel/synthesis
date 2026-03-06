# Synthesis Platform - Complete API Reference

## Overview
Complete API documentation for all endpoints, request/response DTOs, and data models.

**Base URL**: `http://localhost:8080`  
**API Version**: v1  
**Authentication**: JWT Bearer Token (except registration/login)

---

## Table of Contents

1. [Clinic Management](#clinic-management)
2. [Authentication](#authentication)
3. [Patient Management](#patient-management)
4. [Consultations](#consultations)
5. [AI Diagnostics](#ai-diagnostics)
6. [Laboratory](#laboratory)
7. [Voice Transcription](#voice-transcription)
8. [Escalation](#escalation)
9. [Analytics](#analytics)
10. [Data Models (DTOs)](#data-models-dtos)

---

## Clinic Management

### Register Clinic
**Endpoint**: `POST /api/v1/clinics/register`  
**Auth**: Not required  
**Description**: Register a new clinic/health facility with admin account

**Request Body**:
```json
{
  "clinicName": "string (required)",
  "clinicAddress": "string (optional)",
  "clinicRegion": "string (optional)",
  "adminName": "string (required)",
  "adminEmail": "string (required)",
  "adminPassword": "string (required)"
}
```

**Response** (201 Created):
```json
{
  "token": "string",
  "email": "string",
  "name": "string",
  "role": "string"
}
```

---

### Get Clinic Details
**Endpoint**: `GET /api/v1/clinics/{clinicId}`  
**Auth**: Required  
**Description**: Retrieve clinic information

**Response** (200 OK):
```json
{
  "id": "number",
  "name": "string",
  "address": "string",
  "region": "string",
  "registrationCode": "string",
  "createdAt": "datetime",
  "staff": []
}
```

---

### Get Clinic Staff
**Endpoint**: `GET /api/v1/clinics/{clinicId}/staff`  
**Auth**: Required  
**Description**: List all staff members at clinic

**Response** (200 OK):
```json
{
  "id": "number",
  "name": "string",
  "address": "string",
  "region": "string",
  "registrationCode": "string",
  "createdAt": "datetime",
  "staff": [
    {
      "id": "number",
      "name": "string",
      "role": "string",
      "email": "string"
    }
  ]
}
```

---

### List All Clinics
**Endpoint**: `GET /api/v1/clinics`  
**Auth**: Required  
**Description**: Get all registered clinics

**Response** (200 OK):
```json
[
  {
    "id": "number",
    "name": "string",
    "address": "string",
    "region": "string",
    "registrationCode": "string",
    "createdAt": "datetime",
    "staff": []
  }
]
```

---

## Authentication

### Register Provider
**Endpoint**: `POST /api/v1/auth/register`  
**Auth**: Not required  
**Description**: Register individual healthcare provider

**Request Body**:
```json
{
  "name": "string (required)",
  "role": "string (required)",
  "clinicName": "string (required)",
  "region": "string (required)",
  "email": "string (required)",
  "password": "string (required)"
}
```

**Response** (201 Created):
```json
{
  "token": "string",
  "email": "string",
  "name": "string",
  "role": "string"
}
```

---

### Login
**Endpoint**: `POST /api/v1/auth/login`  
**Auth**: Not required  
**Description**: Authenticate existing provider

**Request Body**:
```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```

**Response** (200 OK):
```json
{
  "token": "string",
  "email": "string",
  "name": "string",
  "role": "string"
}
```

---

## Patient Management

### Create Patient
**Endpoint**: `POST /api/v1/patients`  
**Auth**: Required  
**Description**: Register new patient

**Request Body**:
```json
{
  "firstName": "string (required)",
  "lastName": "string (required)",
  "dateOfBirth": "date (required, format: YYYY-MM-DD)",
  "gender": "enum (required: MALE, FEMALE, OTHER)",
  "nationalId": "string (optional)",
  "bloodGroup": "enum (optional: A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE, AB_POSITIVE, AB_NEGATIVE, O_POSITIVE, O_NEGATIVE)",
  "allergies": "string (optional)",
  "clinicName": "string (optional)"
}
```

**Response** (201 Created):
```json
{
  "id": "number",
  "firstName": "string",
  "lastName": "string",
  "dateOfBirth": "date",
  "gender": "string",
  "nationalId": "string",
  "bloodGroup": "string",
  "allergies": "string",
  "clinicName": "string"
}
```

---

### Get Patient
**Endpoint**: `GET /api/v1/patients/{id}`  
**Auth**: Required  
**Description**: Retrieve patient details

**Response** (200 OK): Same as Create Patient response

---

### Update Patient
**Endpoint**: `PUT /api/v1/patients/{id}`  
**Auth**: Required  
**Description**: Update patient information

**Request Body**: Same as Create Patient  
**Response** (200 OK): Same as Create Patient response

---

### List Patients
**Endpoint**: `GET /api/v1/patients`  
**Auth**: Required  
**Description**: Get paginated list of patients

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response** (200 OK):
```json
{
  "content": [/* array of PatientResponse */],
  "totalElements": "number",
  "totalPages": "number",
  "size": "number",
  "number": "number"
}
```

---

### Search Patients
**Endpoint**: `GET /api/v1/patients/search`  
**Auth**: Required  
**Description**: Search patients by name or national ID

**Query Parameters**:
- `q` (required): Search query
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response** (200 OK): Same as List Patients

---

## Consultations

### Open Consultation
**Endpoint**: `POST /api/v1/consultations`  
**Auth**: Required  
**Description**: Start new clinical consultation

**Request Body**:
```json
{
  "patientId": "number (required)",
  "chiefComplaint": "string (required)",
  "vitals": "string (optional, JSON format)",
  "notes": "string (optional)"
}
```

**Response** (201 Created):
```json
{
  "id": "number",
  "patientId": "number",
  "patientName": "string",
  "providerId": "number",
  "providerName": "string",
  "status": "enum (OPEN, IN_PROGRESS, CLOSED)",
  "chiefComplaint": "string",
  "vitals": "string",
  "notes": "string",
  "openedAt": "datetime",
  "closedAt": "datetime (nullable)",
  "diagnoses": []
}
```

---

### Update Consultation
**Endpoint**: `PUT /api/v1/consultations/{id}`  
**Auth**: Required  
**Description**: Update consultation details

**Request Body**:
```json
{
  "chiefComplaint": "string (optional)",
  "vitals": "string (optional)",
  "notes": "string (optional)",
  "status": "enum (optional: OPEN, IN_PROGRESS, CLOSED)"
}
```

**Response** (200 OK): Same as Open Consultation response

---

### Close Consultation
**Endpoint**: `POST /api/v1/consultations/{id}/close`  
**Auth**: Required  
**Description**: Finalize consultation (immutable after)

**Response** (200 OK): Same as Open Consultation response with status=CLOSED

---

### Get Consultation
**Endpoint**: `GET /api/v1/consultations/{id}`  
**Auth**: Required  
**Description**: Retrieve consultation details

**Response** (200 OK): Same as Open Consultation response

---

### Get Consultation History
**Endpoint**: `GET /api/v1/consultations/{id}/history`  
**Auth**: Required  
**Description**: Get consultation timeline (currently same as Get Consultation)

**Response** (200 OK): Same as Open Consultation response

---

### Get Active Consultations
**Endpoint**: `GET /api/v1/consultations/provider/active`  
**Auth**: Required  
**Description**: List provider's open/in-progress consultations

**Response** (200 OK):
```json
[
  /* array of ConsultationResponse */
]
```

---

### Get Patient History
**Endpoint**: `GET /api/v1/consultations/patient/{patientId}/history`  
**Auth**: Required  
**Description**: Get all consultations for a patient

**Response** (200 OK):
```json
[
  /* array of ConsultationResponse */
]
```

---

## AI Diagnostics

### Analyze Symptoms (Text)
**Endpoint**: `POST /api/v1/diagnostic/analyze`  
**Auth**: Required  
**Description**: Generate AI differential diagnosis with RAG

**Request Body**:
```json
{
  "consultationId": "number (required)",
  "availableEquipment": ["string array (optional)"],
  "localFormulary": ["string array (optional)"],
  "additionalNotes": "string (optional)"
}
```

**Response** (200 OK):
```json
{
  "consultationId": "number",
  "differentials": [
    {
      "condition": "string",
      "confidence": "number (0.0-1.0)",
      "reasoning": "string",
      "recommendedTests": ["string array"],
      "redFlags": ["string array"]
    }
  ],
  "immediateActions": ["string array"],
  "safetyNotes": "string",
  "citations": ["string array"],
  "generatedAt": "datetime"
}
```

---

### Analyze Image (Multimodal)
**Endpoint**: `POST /api/v1/diagnostic/analyze-image`  
**Auth**: Required  
**Description**: Analyze medical images using Claude 3 vision

**Content-Type**: `multipart/form-data`

**Form Data**:
- `image` (required): File (JPEG/PNG, max 5MB)
- `clinicalContext` (optional): String

**Response** (200 OK):
```json
{
  "description": "string",
  "findings": ["string array"],
  "analyzedAt": "datetime"
}
```

---

### Generate Treatment Plan
**Endpoint**: `POST /api/v1/diagnostic/treatment`  
**Auth**: Required  
**Description**: Create context-aware treatment plan

**Request Body**:
```json
{
  "consultationId": "number (required)",
  "confirmedDiagnosis": "string (required)",
  "patientWeight": "number (optional)",
  "localFormulary": ["string array (optional)"],
  "availableResources": "string (optional)",
  "additionalContext": "string (optional)"
}
```

**Response** (200 OK):
```json
{
  "consultationId": "number",
  "diagnosis": "string",
  "treatments": [
    {
      "drugName": "string",
      "dosage": "string",
      "route": "string",
      "frequency": "string",
      "duration": "string",
      "instructions": "string",
      "precautions": "string"
    }
  ],
  "followUpSchedule": [
    {
      "timepoint": "string",
      "action": "string"
    }
  ],
  "patientEducation": ["string array"],
  "escalationCriteria": ["string array"],
  "generatedAt": "datetime"
}
```

---

### Get Treatments by Diagnosis
**Endpoint**: `GET /api/v1/diagnostic/treatment/{diagnosisId}`  
**Auth**: Required  
**Description**: Retrieve treatments for specific diagnosis

**Response** (200 OK):
```json
[
  {
    "id": "number",
    "drugName": "string",
    "dosage": "string",
    "duration": "string",
    "instructions": "string",
    "type": "string"
  }
]
```

---

## Laboratory

### Add Lab Result
**Endpoint**: `POST /api/v1/labs`  
**Auth**: Required  
**Description**: Submit laboratory test results

**Request Body**:
```json
{
  "consultationId": "number (required)",
  "testName": "string (required)",
  "testType": "enum (required: HEMATOLOGY, BIOCHEMISTRY, MICROBIOLOGY, SEROLOGY, URINALYSIS, IMAGING)",
  "result": "string (required)",
  "unit": "string (optional)",
  "referenceRange": "string (optional)",
  "interpretation": "string (optional)",
  "performedAt": "datetime (optional)"
}
```

**Response** (201 Created):
```json
{
  "id": "number",
  "consultationId": "number",
  "testName": "string",
  "testType": "string",
  "result": "string",
  "unit": "string",
  "referenceRange": "string",
  "interpretation": "string",
  "performedAt": "datetime",
  "createdAt": "datetime"
}
```

---

### Get Lab Results by Consultation
**Endpoint**: `GET /api/v1/labs/consultation/{consultationId}`  
**Auth**: Required  
**Description**: Retrieve all lab results for consultation

**Response** (200 OK):
```json
[
  /* array of LabResultResponse */
]
```

---

## Voice Transcription

### Transcribe Audio
**Endpoint**: `POST /api/v1/transcribe`  
**Auth**: Required  
**Description**: Convert audio to text using AWS Transcribe

**Content-Type**: `multipart/form-data`

**Form Data**:
- `audio` (required): File (WAV, MP3, MP4, M4A)

**Response** (200 OK):
```json
{
  "transcription": "string",
  "confidence": "number (0.0-1.0)",
  "language": "string",
  "duration": "number (seconds)",
  "transcribedAt": "datetime"
}
```

---

## Escalation

### Refer to Specialist
**Endpoint**: `POST /api/v1/escalation/refer`  
**Auth**: Required  
**Description**: Escalate case with AI-generated summary

**Request Body**:
```json
{
  "consultationId": "number (required)",
  "urgencyLevel": "enum (required: ROUTINE, URGENT, EMERGENCY)",
  "specialistType": "string (required)",
  "reason": "string (required)",
  "preferredContactMethod": "enum (optional: PHONE, EMAIL, TELEMEDICINE)",
  "additionalNotes": "string (optional)"
}
```

**Response** (201 Created):
```json
{
  "id": "number",
  "referralId": "string",
  "consultationId": "number",
  "patientName": "string",
  "urgencyLevel": "string",
  "specialistType": "string",
  "status": "string",
  "caseSummary": {
    "patientDemographics": {},
    "clinicalPresentation": {},
    "diagnosis": {},
    "investigations": {},
    "treatment": {},
    "redFlags": [],
    "clinicCapabilities": "string",
    "reasonForReferral": "string"
  },
  "createdAt": "datetime",
  "expectedResponseTime": "datetime",
  "notificationSent": "boolean"
}
```

---

## Analytics

### Get Disease Trends
**Endpoint**: `GET /api/v1/analytics/trends`  
**Auth**: Required  
**Description**: Monitor disease patterns over time

**Query Parameters**:
- `region` (optional): Filter by region
- `from` (optional): Start date (YYYY-MM-DD)
- `to` (optional): End date (YYYY-MM-DD)

**Response** (200 OK):
```json
[
  {
    "condition": "string",
    "caseCount": "number",
    "period": "string",
    "region": "string",
    "trend": "enum (INCREASING, STABLE, DECREASING)",
    "percentageChange": "number"
  }
]
```

---

### Get Treatment Outcomes
**Endpoint**: `GET /api/v1/analytics/outcomes`  
**Auth**: Required  
**Description**: Track treatment success rates

**Response** (200 OK):
```json
[
  {
    "condition": "string",
    "totalCases": "number",
    "resolved": "number",
    "escalated": "number",
    "ongoing": "number",
    "successRate": "number",
    "averageConfidence": "number"
  }
]
```

---

### Get Clinic Summary
**Endpoint**: `GET /api/v1/analytics/clinic/{clinicName}`  
**Auth**: Required  
**Description**: Clinic performance dashboard

**Response** (200 OK):
```json
{
  "clinicName": "string",
  "period": "string",
  "totalPatients": "number",
  "totalConsultations": "number",
  "activeConsultations": "number",
  "closedConsultations": "number",
  "topConditions": [
    {
      "condition": "string",
      "count": "number",
      "percentage": "number"
    }
  ],
  "providerUtilization": [
    {
      "providerName": "string",
      "consultations": "number",
      "averageConsultationTime": "string"
    }
  ],
  "escalationRate": "number",
  "averageDiagnosticConfidence": "number"
}
```

---

## Data Models (DTOs)

### Request DTOs

#### ClinicRegistrationRequest
```json
{
  "clinicName": "string (required)",
  "clinicAddress": "string (optional)",
  "clinicRegion": "string (optional)",
  "adminName": "string (required)",
  "adminEmail": "string (required)",
  "adminPassword": "string (required)"
}
```

#### RegisterRequest
```json
{
  "name": "string (required)",
  "role": "string (required)",
  "clinicName": "string (required)",
  "region": "string (required)",
  "email": "string (required)",
  "password": "string (required)"
}
```

#### AuthRequest
```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```

#### PatientRequest
```json
{
  "firstName": "string (required)",
  "lastName": "string (required)",
  "dateOfBirth": "date (required)",
  "gender": "enum (required: MALE, FEMALE, OTHER)",
  "nationalId": "string (optional)",
  "bloodGroup": "enum (optional)",
  "allergies": "string (optional)",
  "clinicName": "string (optional)"
}
```

#### ConsultationRequest
```json
{
  "patientId": "number (required)",
  "chiefComplaint": "string (required)",
  "vitals": "string (optional)",
  "notes": "string (optional)"
}
```

#### ConsultationUpdateRequest
```json
{
  "chiefComplaint": "string (optional)",
  "vitals": "string (optional)",
  "notes": "string (optional)",
  "status": "enum (optional)"
}
```

#### DiagnosticRequest
```json
{
  "consultationId": "number (required)",
  "availableEquipment": ["string array (optional)"],
  "localFormulary": ["string array (optional)"],
  "additionalNotes": "string (optional)"
}
```

#### TreatmentRequest
```json
{
  "consultationId": "number (required)",
  "confirmedDiagnosis": "string (required)",
  "patientWeight": "number (optional)",
  "localFormulary": ["string array (optional)"],
  "availableResources": "string (optional)",
  "additionalContext": "string (optional)"
}
```

#### LabResultRequest
```json
{
  "consultationId": "number (required)",
  "testName": "string (required)",
  "testType": "enum (required)",
  "result": "string (required)",
  "unit": "string (optional)",
  "referenceRange": "string (optional)",
  "interpretation": "string (optional)",
  "performedAt": "datetime (optional)"
}
```

#### EscalationRequest
```json
{
  "consultationId": "number (required)",
  "urgencyLevel": "enum (required: ROUTINE, URGENT, EMERGENCY)",
  "specialistType": "string (required)",
  "reason": "string (required)",
  "preferredContactMethod": "enum (optional)",
  "additionalNotes": "string (optional)"
}
```

---

### Response DTOs

#### AuthResponse
```json
{
  "token": "string",
  "email": "string",
  "name": "string",
  "role": "string"
}
```

#### ClinicResponse
```json
{
  "id": "number",
  "name": "string",
  "address": "string",
  "region": "string",
  "registrationCode": "string",
  "createdAt": "datetime",
  "staff": [
    {
      "id": "number",
      "name": "string",
      "role": "string",
      "email": "string"
    }
  ]
}
```

#### PatientResponse
```json
{
  "id": "number",
  "firstName": "string",
  "lastName": "string",
  "dateOfBirth": "date",
  "gender": "string",
  "nationalId": "string",
  "bloodGroup": "string",
  "allergies": "string",
  "clinicName": "string"
}
```

#### ConsultationResponse
```json
{
  "id": "number",
  "patientId": "number",
  "patientName": "string",
  "providerId": "number",
  "providerName": "string",
  "status": "enum",
  "chiefComplaint": "string",
  "vitals": "string",
  "notes": "string",
  "openedAt": "datetime",
  "closedAt": "datetime",
  "diagnoses": [
    {
      "id": "number",
      "conditionName": "string",
      "confidence": "number",
      "reasoning": "string",
      "source": "string"
    }
  ]
}
```

#### DiagnosticResponse
```json
{
  "consultationId": "number",
  "differentials": [
    {
      "condition": "string",
      "confidence": "number",
      "reasoning": "string",
      "recommendedTests": ["string array"],
      "redFlags": ["string array"]
    }
  ],
  "immediateActions": ["string array"],
  "safetyNotes": "string",
  "citations": ["string array"],
  "generatedAt": "datetime"
}
```

#### DifferentialDto
```json
{
  "condition": "string",
  "confidence": "number",
  "reasoning": "string",
  "recommendedTests": ["string array"],
  "redFlags": ["string array"]
}
```

#### DiagnosisResponse
```json
{
  "id": "number",
  "conditionName": "string",
  "confidence": "number",
  "reasoning": "string",
  "recommendedTests": ["string array"],
  "redFlags": ["string array"],
  "source": "string"
}
```

#### ImageAnalysisResponse
```json
{
  "description": "string",
  "findings": ["string array"],
  "analyzedAt": "datetime"
}
```

#### TreatmentPlanResponse
```json
{
  "consultationId": "number",
  "diagnosis": "string",
  "treatments": [
    {
      "drugName": "string",
      "dosage": "string",
      "route": "string",
      "frequency": "string",
      "duration": "string",
      "instructions": "string",
      "precautions": "string"
    }
  ],
  "followUpSchedule": [
    {
      "timepoint": "string",
      "action": "string"
    }
  ],
  "patientEducation": ["string array"],
  "escalationCriteria": ["string array"],
  "generatedAt": "datetime"
}
```

#### TreatmentResponse
```json
{
  "id": "number",
  "drugName": "string",
  "dosage": "string",
  "duration": "string",
  "instructions": "string",
  "type": "string"
}
```

#### LabResultResponse
```json
{
  "id": "number",
  "consultationId": "number",
  "testName": "string",
  "testType": "string",
  "result": "string",
  "unit": "string",
  "referenceRange": "string",
  "interpretation": "string",
  "performedAt": "datetime",
  "createdAt": "datetime"
}
```

#### TranscriptionResponse
```json
{
  "transcription": "string",
  "confidence": "number",
  "language": "string",
  "duration": "number",
  "transcribedAt": "datetime"
}
```

#### EscalationResponse
```json
{
  "id": "number",
  "referralId": "string",
  "consultationId": "number",
  "patientName": "string",
  "urgencyLevel": "string",
  "specialistType": "string",
  "status": "string",
  "caseSummary": "object",
  "createdAt": "datetime",
  "expectedResponseTime": "datetime",
  "notificationSent": "boolean"
}
```

#### TrendResponse
```json
{
  "condition": "string",
  "caseCount": "number",
  "period": "string",
  "region": "string",
  "trend": "enum",
  "percentageChange": "number"
}
```

#### OutcomeResponse
```json
{
  "condition": "string",
  "totalCases": "number",
  "resolved": "number",
  "escalated": "number",
  "ongoing": "number",
  "successRate": "number",
  "averageConfidence": "number"
}
```

#### KnowledgeBaseCitation
```json
{
  "text": "string",
  "source": "string",
  "relevanceScore": "number"
}
```

---

## Enumerations

### Gender
- `MALE`
- `FEMALE`
- `OTHER`

### BloodGroup
- `A_POSITIVE`
- `A_NEGATIVE`
- `B_POSITIVE`
- `B_NEGATIVE`
- `AB_POSITIVE`
- `AB_NEGATIVE`
- `O_POSITIVE`
- `O_NEGATIVE`

### ConsultationStatus
- `OPEN` - Consultation started
- `IN_PROGRESS` - Active diagnosis/treatment
- `CLOSED` - Finalized (immutable)

### UrgencyLevel
- `ROUTINE` - Response within 2-3 days
- `URGENT` - Response within 24 hours
- `EMERGENCY` - Immediate response required

### TestType
- `HEMATOLOGY` - Blood counts, coagulation
- `BIOCHEMISTRY` - Glucose, electrolytes, organ function
- `MICROBIOLOGY` - Cultures, sensitivity
- `SEROLOGY` - Antibody tests, RDTs
- `URINALYSIS` - Urine tests
- `IMAGING` - X-ray, ultrasound reports

### ContactMethod
- `PHONE`
- `EMAIL`
- `TELEMEDICINE`

### Trend
- `INCREASING`
- `STABLE`
- `DECREASING`

---

## Error Responses

All errors return consistent JSON structure:

```json
{
  "code": "string",
  "message": "string",
  "httpStatus": "number",
  "timestamp": "datetime"
}
```

### Error Codes
- `VALIDATION_ERROR` - Invalid input (400)
- `RESOURCE_NOT_FOUND` - Entity not found (404)
- `DIAGNOSTIC_ERROR` - AI analysis failed (500)
- `AUTHENTICATION_ERROR` - Auth failure (401)
- `AUTHORIZATION_ERROR` - Insufficient permissions (403)
- `INTERNAL_ERROR` - Server error (500)

---

## HTTP Status Codes

| Code | Status | Usage |
|------|--------|-------|
| 200 | OK | Successful GET/PUT request |
| 201 | Created | Successful POST (resource created) |
| 400 | Bad Request | Invalid request body/parameters |
| 401 | Unauthorized | Missing/invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource |
| 500 | Internal Server Error | Server error |

---

## Authentication

### JWT Token Format
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA4NTE4NDAwLCJleHAiOjE3MDg2MDQ4MDB9.signature
```

### Token Expiration
- **Validity**: 24 hours
- **Refresh**: Re-login or register to get new token
- **Storage**: Secure storage recommended (not localStorage in production)

### Protected Endpoints
All endpoints except:
- `POST /api/v1/clinics/register`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

---

## Rate Limiting

**Current**: Not implemented  
**Recommended for Production**:
- 100 requests/minute per IP
- 1000 requests/hour per user
- 10 AI analysis requests/minute per user

---

## Pagination

Endpoints supporting pagination:
- `GET /api/v1/patients`
- `GET /api/v1/patients/search`

**Query Parameters**:
- `page` (default: 0) - Zero-indexed page number
- `size` (default: 20) - Items per page

**Response Format**:
```json
{
  "content": [],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

---

## Date/Time Formats

- **Date**: `YYYY-MM-DD` (e.g., `2026-02-19`)
- **DateTime**: ISO 8601 `YYYY-MM-DDTHH:mm:ss` (e.g., `2026-02-19T09:15:00`)
- **Timezone**: UTC (server-side), local timezone conversion on client

---

## File Upload Constraints

### Images (analyze-image)
- **Formats**: JPEG, PNG
- **Max Size**: 5MB
- **Validation**: Client-side compression recommended

### Audio (transcribe)
- **Formats**: WAV, MP3, MP4, M4A
- **Max Size**: 10MB (recommended)
- **Duration**: Up to 5 minutes

---

## Best Practices

1. **Always include Authorization header** for protected endpoints
2. **Validate input client-side** before sending requests
3. **Handle errors gracefully** with user-friendly messages
4. **Implement retry logic** for 5xx errors (exponential backoff)
5. **Cache static data** (formularies, equipment lists)
6. **Use pagination** for large datasets
7. **Compress images** before upload
8. **Log errors** for debugging (exclude sensitive data)

---

## Postman Collection

Import the complete Postman collection from:
- [POSTMAN_PATIENT_HISTORY_API.md](POSTMAN_PATIENT_HISTORY_API.md)
- [COMPLETE_WORKFLOW_GUIDE.md](COMPLETE_WORKFLOW_GUIDE.md)

---

## Support

- **Technical Documentation**: [COMPLETE_WORKFLOW_GUIDE.md](COMPLETE_WORKFLOW_GUIDE.md)
- **Multimodal Features**: [MULTIMODAL_DIAGNOSTICS.md](MULTIMODAL_DIAGNOSTICS.md)
- **RAG Implementation**: [RAG_KNOWLEDGE_BASE.md](RAG_KNOWLEDGE_BASE.md)
- **GitHub Issues**: Report bugs and feature requests
- **Email**: dev@synthesis.health

---

**Document Version**: 1.0  
**Last Updated**: February 19, 2026  
**API Version**: v1  
**Total Endpoints**: 32
