# Patient History Aggregation API - Postman Guide

## Overview
This guide provides complete details for testing the patient consultation history aggregation feature using Postman.

---

## API Endpoint Details

### Get Patient Consultation History

**Endpoint**: `GET /api/v1/consultations/patient/{patientId}/history`

**Description**: Retrieves all consultation records for a specific patient, aggregating their complete medical history including diagnoses, treatments, and clinical notes.

**Authentication**: Required (JWT Bearer Token)

---

## Step-by-Step Postman Setup

### Step 1: Register a Provider (Get JWT Token)

**Method**: `POST`  
**URL**: `http://localhost:8080/api/v1/auth/register`

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Dr. Sarah Johnson",
  "role": "General Practitioner",
  "clinicName": "City Health Clinic",
  "region": "North Region",
  "email": "sarah.johnson@clinic.com",
  "password": "SecurePassword123!"
}
```

**Expected Response** (201 Created):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYXJhaC5qb2huc29uQGNsaW5pYy5jb20iLCJpYXQiOjE3MDg0MzIwMDAsImV4cCI6MTcwODUxODQwMH0.xyz123...",
  "email": "sarah.johnson@clinic.com",
  "name": "Dr. Sarah Johnson",
  "role": "General Practitioner"
}
```

**Action**: Copy the `token` value for use in subsequent requests.

---

### Step 2: Create a Test Patient

**Method**: `POST`  
**URL**: `http://localhost:8080/api/v1/patients`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYXJhaC5qb2huc29uQGNsaW5pYy5jb20iLCJpYXQiOjE3MDg0MzIwMDAsImV4cCI6MTcwODUxODQwMH0.xyz123...
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
  "clinicName": "City Health Clinic"
}
```

**Expected Response** (201 Created):
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Kamau",
  "dateOfBirth": "1985-03-15",
  "gender": "MALE",
  "nationalId": "12345678",
  "bloodGroup": "O_POSITIVE",
  "allergies": "Penicillin",
  "clinicName": "City Health Clinic"
}
```

**Action**: Note the patient `id` (e.g., 1) for creating consultations.

---

### Step 3: Create Multiple Consultations for the Patient

#### Consultation 1 - Malaria Case

**Method**: `POST`  
**URL**: `http://localhost:8080/api/v1/consultations`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYXJhaC5qb2huc29uQGNsaW5pYy5jb20iLCJpYXQiOjE3MDg0MzIwMDAsImV4cCI6MTcwODUxODQwMH0.xyz123...
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

**Expected Response** (201 Created):
```json
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
  "openedAt": "2026-02-19T10:30:00",
  "closedAt": null,
  "diagnoses": []
}
```

#### Consultation 2 - Follow-up Visit

**Method**: `POST`  
**URL**: `http://localhost:8080/api/v1/consultations`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
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

#### Consultation 3 - Respiratory Infection

**Method**: `POST`  
**URL**: `http://localhost:8080/api/v1/consultations`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <your_token_here>
```

**Request Body**:
```json
{
  "patientId": 1,
  "chiefComplaint": "Persistent cough and chest pain for 2 weeks",
  "vitals": "{\"temperature\": 38.5, \"bloodPressure\": \"125/82\", \"heartRate\": 88, \"spO2\": 94}",
  "notes": "Productive cough with yellow sputum. Chest pain on deep breathing. Patient is a smoker (10 cigarettes/day)."
}
```

---

### Step 4: Get Patient Consultation History (Main Feature)

**Method**: `GET`  
**URL**: `http://localhost:8080/api/v1/consultations/patient/1/history`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYXJhaC5qb2huc29uQGNsaW5pYy5jb20iLCJpYXQiOjE3MDg0MzIwMDAsImV4cCI6MTcwODUxODQwMH0.xyz123...
```

**Query Parameters**: None

**Request Body**: None (GET request)

**Expected Response** (200 OK):
```json
[
  {
    "id": 1,
    "patientId": 1,
    "patientName": "John Kamau",
    "providerId": 1,
    "providerName": "Dr. Sarah Johnson",
    "status": "CLOSED",
    "chiefComplaint": "Fever, chills, and headache for 3 days",
    "vitals": "{\"temperature\": 39.2, \"bloodPressure\": \"120/80\", \"heartRate\": 98, \"spO2\": 97}",
    "notes": "Patient reports recent travel to malaria-endemic region. Night sweats present.",
    "openedAt": "2026-02-19T10:30:00",
    "closedAt": "2026-02-19T11:15:00",
    "diagnoses": [
      {
        "id": 1,
        "conditionName": "Malaria (Plasmodium falciparum)",
        "confidence": 0.85,
        "reasoning": "Patient presents with classic malaria symptoms: fever, chills, headache, and recent travel to endemic area. Night sweats are a red flag for severe malaria.",
        "recommendedTests": ["Blood smear", "Rapid diagnostic test (RDT)"],
        "redFlags": ["High fever >39°C", "Night sweats"],
        "source": "AI_BEDROCK_RAG"
      }
    ]
  },
  {
    "id": 2,
    "patientId": 1,
    "patientName": "John Kamau",
    "providerId": 1,
    "providerName": "Dr. Sarah Johnson",
    "status": "CLOSED",
    "chiefComplaint": "Follow-up for malaria treatment",
    "vitals": "{\"temperature\": 37.1, \"bloodPressure\": \"118/78\", \"heartRate\": 72, \"spO2\": 98}",
    "notes": "Patient completed 3-day artemisinin course. Symptoms resolved. No fever for 48 hours.",
    "openedAt": "2026-02-22T09:00:00",
    "closedAt": "2026-02-22T09:20:00",
    "diagnoses": []
  },
  {
    "id": 3,
    "patientId": 1,
    "patientName": "John Kamau",
    "providerId": 1,
    "providerName": "Dr. Sarah Johnson",
    "status": "OPEN",
    "chiefComplaint": "Persistent cough and chest pain for 2 weeks",
    "vitals": "{\"temperature\": 38.5, \"bloodPressure\": \"125/82\", \"heartRate\": 88, \"spO2\": 94}",
    "notes": "Productive cough with yellow sputum. Chest pain on deep breathing. Patient is a smoker (10 cigarettes/day).",
    "openedAt": "2026-03-01T14:30:00",
    "closedAt": null,
    "diagnoses": []
  }
]
```

---

## Complete Postman Collection JSON

You can import this JSON directly into Postman:

```json
{
  "info": {
    "name": "Synthesis - Patient History API",
    "description": "Complete API collection for testing patient consultation history aggregation",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080",
      "type": "string"
    },
    {
      "key": "token",
      "value": "",
      "type": "string"
    },
    {
      "key": "patientId",
      "value": "",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "1. Register Provider",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "if (pm.response.code === 201) {",
              "    var jsonData = pm.response.json();",
              "    pm.collectionVariables.set('token', jsonData.token);",
              "    pm.environment.set('token', jsonData.token);",
              "}"
            ],
            "type": "text/javascript"
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"Dr. Sarah Johnson\",\n  \"role\": \"General Practitioner\",\n  \"clinicName\": \"City Health Clinic\",\n  \"region\": \"North Region\",\n  \"email\": \"sarah.johnson@clinic.com\",\n  \"password\": \"SecurePassword123!\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/v1/auth/register",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "auth", "register"]
        }
      }
    },
    {
      "name": "2. Create Patient",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "if (pm.response.code === 201) {",
              "    var jsonData = pm.response.json();",
              "    pm.collectionVariables.set('patientId', jsonData.id);",
              "    pm.environment.set('patientId', jsonData.id);",
              "}"
            ],
            "type": "text/javascript"
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"firstName\": \"John\",\n  \"lastName\": \"Kamau\",\n  \"dateOfBirth\": \"1985-03-15\",\n  \"gender\": \"MALE\",\n  \"nationalId\": \"12345678\",\n  \"bloodGroup\": \"O_POSITIVE\",\n  \"allergies\": \"Penicillin\",\n  \"clinicName\": \"City Health Clinic\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/v1/patients",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "patients"]
        }
      }
    },
    {
      "name": "3. Create Consultation - Malaria",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"patientId\": {{patientId}},\n  \"chiefComplaint\": \"Fever, chills, and headache for 3 days\",\n  \"vitals\": \"{\\\"temperature\\\": 39.2, \\\"bloodPressure\\\": \\\"120/80\\\", \\\"heartRate\\\": 98, \\\"spO2\\\": 97}\",\n  \"notes\": \"Patient reports recent travel to malaria-endemic region. Night sweats present.\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/v1/consultations",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "consultations"]
        }
      }
    },
    {
      "name": "4. Create Consultation - Follow-up",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"patientId\": {{patientId}},\n  \"chiefComplaint\": \"Follow-up for malaria treatment\",\n  \"vitals\": \"{\\\"temperature\\\": 37.1, \\\"bloodPressure\\\": \\\"118/78\\\", \\\"heartRate\\\": 72, \\\"spO2\\\": 98}\",\n  \"notes\": \"Patient completed 3-day artemisinin course. Symptoms resolved.\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/v1/consultations",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "consultations"]
        }
      }
    },
    {
      "name": "5. Get Patient History",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{token}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/v1/consultations/patient/{{patientId}}/history",
          "host": ["{{baseUrl}}"],
          "path": ["api", "v1", "consultations", "patient", "{{patientId}}", "history"]
        }
      }
    }
  ]
}
```

---

## Quick Reference Card

### Endpoint Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/auth/register` | Get JWT token |
| POST | `/api/v1/patients` | Create patient |
| POST | `/api/v1/consultations` | Create consultation |
| GET | `/api/v1/consultations/patient/{patientId}/history` | **Get patient history** |

### Required Headers

```
Content-Type: application/json
Authorization: Bearer <your_jwt_token>
```

### Common Response Codes

- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request body or parameters
- `401 Unauthorized` - Missing or invalid JWT token
- `404 Not Found` - Patient or consultation not found
- `500 Internal Server Error` - Server error

---

## Testing Scenarios

### Scenario 1: Patient with No History
**Test**: Request history for a newly created patient with no consultations  
**Expected**: Empty array `[]`

### Scenario 2: Patient with Multiple Consultations
**Test**: Request history for patient with 3+ consultations  
**Expected**: Array with all consultations ordered by `openedAt` (most recent first)

### Scenario 3: Patient with Diagnoses
**Test**: Request history for patient with consultations that have AI diagnoses  
**Expected**: Each consultation includes `diagnoses` array with diagnosis details

### Scenario 4: Invalid Patient ID
**Test**: Request history with non-existent patient ID (e.g., 99999)  
**Expected**: `200 OK` with empty array `[]` (or `404 Not Found` depending on implementation)

### Scenario 5: Unauthorized Access
**Test**: Request without Authorization header  
**Expected**: `401 Unauthorized`

---

## Troubleshooting

### Issue: 401 Unauthorized
**Solution**: Ensure you've included the `Authorization: Bearer <token>` header and the token hasn't expired (24-hour validity).

### Issue: 404 Not Found
**Solution**: Verify the patient ID exists by first calling `GET /api/v1/patients/{id}`.

### Issue: Empty Response
**Solution**: Ensure you've created consultations for the patient using `POST /api/v1/consultations`.

### Issue: Token Expired
**Solution**: Re-register or login to get a new token:
```
POST /api/v1/auth/login
{
  "email": "sarah.johnson@clinic.com",
  "password": "SecurePassword123!"
}
```

---

## Additional Notes

1. **Date Format**: All timestamps use ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`
2. **Vitals Format**: Stored as JSON string, parse on client side
3. **Pagination**: Currently not implemented, returns all consultations
4. **Sorting**: Results are ordered by `openedAt` timestamp (most recent first)
5. **Security**: All endpoints except `/auth/register` and `/auth/login` require JWT authentication

---

**Document Version**: 1.0  
**Last Updated**: February 19, 2026  
**API Version**: v1
