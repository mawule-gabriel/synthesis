# Patient History Aggregation API - Postman Guide

## Overview
This guide provides complete details for testing the patient consultation history aggregation feature using Postman, starting from creating clinics and providers, all the way through to retrieving a patient's full history.

---

## API Endpoint Details

### Get Patient Consultation History

**Endpoint**: `GET /api/v1/consultations/patient/{patientId}/history`

**Description**: Retrieves all consultation records for a specific patient, aggregating their complete medical history including diagnoses, treatments, and clinical notes.

**Authentication**: Required (JWT Bearer Token)

**Access Control**: 
- Providers can only fetch history for patients registered to their own clinic.
- Attempting to access a patient from another clinic returns `403 Forbidden`.
- Providers with the `SUPER_ADMIN` role bypass this restriction.

---

## Step-by-Step Postman Setup

### Step 1: Register a Clinic (Creates CLINIC_ADMIN)

**Method**: `POST`  
**URL**: `http://localhost:8080/api/v1/clinics/register`

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "clinicName": "City Health Clinic",
  "clinicAddress": "123 Health Ave",
  "clinicRegion": "North Region",
  "adminName": "Dr. Sarah Johnson",
  "adminEmail": "sarah.johnson@clinic.com",
  "adminPassword": "SecurePassword123!"
}
```

**Expected Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "sarah.johnson@clinic.com",
  "name": "Dr. Sarah Johnson",
  "role": "CLINIC_ADMIN",
  "clinicId": 1,
  "clinicName": "City Health Clinic",
  "registrationCode": "ABC12345"
}
```

**Action**: Copy the `token` value for use in Step 3. Note the `clinicId`.

---

### Step 2: Login as the Default SUPER_ADMIN (Cross-Clinic Inspector)

**Method**: `POST`  
**URL**: `http://localhost:8080/api/v1/auth/login`

**Description**: For security, `SUPER_ADMIN` accounts cannot be registered publicly. A default system super admin is seeded automatically at application startup. They have access to all patients across all clinics.

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "superadmin@synthesis.com",
  "password": "SuperAdmin123!"
}
```

**Expected Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "superadmin@synthesis.com",
  "name": "System Super Admin",
  "role": "SUPER_ADMIN"
}
```

**Action**: Save this `SUPER_ADMIN` token to test the bypass later.

---

### Step 3: Create a Test Patient (Under City Health Clinic)

**Method**: `POST`  
**URL**: `http://localhost:8080/api/v1/patients`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <token_from_step_1>
```

**Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Kamau",
  "dateOfBirth": "1985-03-15",
  "gender": "MALE",
  "nationalId": "12345678",
  "bloodGroup": "O_POSITIVE",
  "allergies": "Penicillin",
  "clinicId": 1
}
```

**Expected Response** (201 Created):
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Kamau",
  "clinicId": 1,
  "clinicName": "City Health Clinic"
}
```

**Action**: Note the patient `id` (e.g., `1`) for creating consultations.

---

### Step 4: Create Consultations for the Patient

#### Consultation 1 - Malaria Case

**Method**: `POST`  
**URL**: `http://localhost:8080/api/v1/consultations`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <token_from_step_1>
```

**Request Body**:
```json
{
  "patientId": 1,
  "chiefComplaint": "Fever, chills, and headache for 3 days",
  "vitals": "{\"temperature\": 39.2, \"bloodPressure\": \"120/80\", \"heartRate\": 98, \"spO2\": 97}",
  "notes": "Patient reports recent travel to malaria-endemic region. Night sweats present."
}
```

#### Consultation 2 - Follow-up Visit

**Method**: `POST`  
**URL**: `http://localhost:8080/api/v1/consultations`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <token_from_step_1>
```

**Request Body**:
```json
{
  "patientId": 1,
  "chiefComplaint": "Follow-up for malaria treatment",
  "vitals": "{\"temperature\": 37.1, \"bloodPressure\": \"118/78\", \"heartRate\": 72, \"spO2\": 98}",
  "notes": "Patient completed 3-day artemisinin course. Symptoms resolved. No fever for 48 hours."
}
```

---

### Step 5: Get Patient Consultation History (Main Feature)

**Method**: `GET`  
**URL**: `http://localhost:8080/api/v1/consultations/patient/1/history`

**Headers**:
```
Authorization: Bearer <token_from_step_1>
```

**Expected Response** (200 OK):
```json
[
  {
    "id": 1,
    "patientId": 1,
    "patientName": "John Kamau",
    "providerId": 1,
    "providerName": "Dr. Sarah Johnson",
    "status": "OPEN",
    "chiefComplaint": "Fever, chills, and headache for 3 days",
    "vitals": "{\"temperature\": 39.2, \"bloodPressure\": \"120/80\", \"heartRate\": 98, \"spO2\": 97}",
    "notes": "Patient reports recent travel to malaria-endemic region. Night sweats present.",
    "openedAt": "2026-03-01T10:30:00",
    "diagnoses": []
  },
  {
    "id": 2,
    "patientId": 1,
    "patientName": "John Kamau",
    "providerId": 1,
    "providerName": "Dr. Sarah Johnson",
    "status": "OPEN",
    "chiefComplaint": "Follow-up for malaria treatment",
    "vitals": "{\"temperature\": 37.1}",
    "notes": "Patient completed course.",
    "openedAt": "2026-03-03T09:00:00",
    "diagnoses": []
  }
]
```

---

## Testing Scenarios

### Scenario 1: Clinic Isolation (Cross-Clinic Access Denied)
**Test**: 
1. Register a **second clinic** (`POST /api/v1/clinics/register`) and get its admin token (`Token B`).
2. Use `Token B` to call `GET /api/v1/consultations/patient/1/history` (where patient 1 belongs to the first clinic).
**Expected**: `403 Forbidden` 
```json
{
  "code": "CLINIC_ACCESS_DENIED",
  "message": "Access denied: patient belongs to a different clinic"
}
```

### Scenario 2: SUPER_ADMIN Bypass
**Test**: 
1. Use the `SUPER_ADMIN` token generated in **Step 2**.
2. Call `GET /api/v1/consultations/patient/1/history`.
**Expected**: `200 OK` (Full history returned regardless of clinic boundary).

### Scenario 3: Unauthorized Access
**Test**: Request without `Authorization` header.
**Expected**: `401 Unauthorized`

---

## Troubleshooting

### Issue: 403 Forbidden (Clinic Access Denied)
**Solution**: The patient you are querying belongs to a different clinic. Ensure you are using the correct token for the patient's clinic, or use a `SUPER_ADMIN` token if cross-clinic access is needed.

### Issue: 401 Unauthorized
**Solution**: Ensure you've included the `Authorization: Bearer <token>` header and the token hasn't expired.

### Issue: Empty Response
**Solution**: Ensure you actually created the consultations for the patient using `POST /api/v1/consultations` before fetching the history.

---

**Document Version**: 1.2  
**API Version**: v1
