# SYNTHESIS
## AI-Powered Clinical Decision Support Platform
### AWS Hackathon — Kiro Development Blueprint

> **Base Package:** `com.asakaa.synthesis` | **Stack:** Java 21 · Spring Boot 3.5 · PostgreSQL · AWS
> **February 2026**

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Package Architecture](#2-package-architecture)
3. [Core Feature Modules](#3-core-feature-modules)
4. [Data Model](#4-data-model-key-entities)
5. [REST API Contract](#5-rest-api-contract)
6. [AWS Integration](#6-aws-integration-hackathon-key)
7. [Kiro Task Sequence](#7-kiro-task-sequence)
8. [Configuration Guide](#8-configuration-guide)
9. [Testing Strategy](#9-testing-strategy)
10. [Development Conventions](#10-development-conventions)
11. [Hackathon Demo Flow](#11-hackathon-demo-flow)
12. [Quick Start Checklist](#12-quick-start-checklist)

---

## 1. Project Overview

Synthesis is a cloud-based AI diagnostic assistant that puts specialist-level clinical reasoning directly into the hands of general practitioners and community health workers in resource-constrained environments. The platform is conversational, evidence-based, and context-aware — adapting its guidance to locally available medications, equipment, and infrastructure.

### Mission Statement

> Make specialist medical expertise universally accessible — starting with the 5 billion people in underserved communities who currently have none.

### Hackathon Scope (Phase 1 Focus)

For the AWS Hackathon we build a demonstrable MVP covering the five highest-impact preventable conditions in underserved regions:

- **Malaria** — Symptom-guided diagnosis + treatment
- **Pneumonia** — Chest assessment + antibiotic recommendation
- **Tuberculosis** — Risk scoring + sputum/imaging guidance
- **Pregnancy Complications** — Pre-eclampsia, haemorrhage, sepsis triage
- **Diabetes / Metabolic** — Type 2 screening + management planning

Every other spec from the hackathon brief — specialist escalation, population analytics, low-bandwidth UI, telemedicine handoff — is architected into the system from day one so the codebase scales naturally into Phase 2+.

---

## 2. Package Architecture

All source lives under `com.asakaa.synthesis`. The structure follows the classic layered Spring Boot pattern extended with domain-specific modules for AI, clinical data, and analytics.

### High-Level Layer Map

| Layer | Package | Responsibility |
|---|---|---|
| API / Controller | `...controller` | HTTP endpoints, request/response mapping, validation |
| Service | `...service` | Business logic, orchestration, AI integration |
| Repository | `...repository` | Spring Data JPA interfaces, custom JPQL queries |
| Domain / Entity | `...domain.entity` | JPA entities, value objects |
| DTO | `...domain.dto` | Request/Response transfer objects (no entities leak out) |
| AI / External | `...integration` | AWS Bedrock client, external API adapters |
| Utility | `...util` | Shared helpers: date, string, medical calc utils |
| Configuration | `...config` | Spring beans, CORS, security, Bedrock config |
| Exception | `...exception` | Custom exceptions + global @ControllerAdvice handler |

### Full Package Tree

```
com.asakaa.synthesis/
├── SynthesisApplication.java
│
├── config/
│   ├── SecurityConfig.java
│   ├── BedrockConfig.java
│   ├── CorsConfig.java
│   └── JpaConfig.java
│
├── controller/
│   ├── DiagnosticController.java
│   ├── PatientController.java
│   ├── ConsultationController.java
│   ├── AnalyticsController.java
│   └── EscalationController.java
│
├── service/
│   ├── DiagnosticService.java
│   ├── PatientService.java
│   ├── ConsultationService.java
│   ├── AnalyticsService.java
│   ├── EscalationService.java
│   └── NotificationService.java
│
├── repository/
│   ├── PatientRepository.java
│   ├── ConsultationRepository.java
│   ├── DiagnosisRepository.java
│   ├── TreatmentRepository.java
│   └── ProviderRepository.java
│
├── domain/
│   ├── entity/
│   │   ├── Patient.java
│   │   ├── Consultation.java
│   │   ├── Diagnosis.java
│   │   ├── Treatment.java
│   │   └── Provider.java
│   └── dto/
│       ├── request/
│       │   ├── DiagnosticRequest.java
│       │   ├── PatientRequest.java
│       │   └── EscalationRequest.java
│       └── response/
│           ├── DiagnosticResponse.java
│           ├── DifferentialDiagnosisResponse.java
│           └── TreatmentPlanResponse.java
│
├── integration/
│   ├── bedrock/
│   │   ├── BedrockClient.java
│   │   └── BedrockPromptBuilder.java
│   └── telemedicine/
│       └── TelemedicineAdapter.java
│
├── util/
│   ├── MedicalCalculator.java
│   ├── DateUtil.java
│   ├── PromptUtil.java
│   └── ResponseParser.java
│
└── exception/
    ├── GlobalExceptionHandler.java
    ├── DiagnosticException.java
    └── ResourceNotFoundException.java
```

---

## 3. Core Feature Modules

Each feature maps to a vertical slice: **Controller → Service → Repository → Entity**. Kiro should generate and wire each slice as a unit.

---

### Module 1 — Intelligent Diagnostic Assistant

#### What it does

Accepts patient demographics, chief complaint, vitals, available lab/imaging results and returns a ranked differential diagnosis with confidence scores and next-step recommendations.

#### Key Classes

| Class | Type | Key Responsibility |
|---|---|---|
| `DiagnosticController` | Controller | `POST /api/v1/diagnostic/analyze` — accepts DiagnosticRequest |
| `DiagnosticService` | Service | Validates inputs, builds AI prompt, calls BedrockClient, parses response |
| `BedrockClient` | Integration | Invokes AWS Bedrock (Claude model) with structured prompt |
| `BedrockPromptBuilder` | Integration | Constructs evidence-based clinical prompts from patient data |
| `ResponseParser` | Util | Parses Bedrock JSON/text into DifferentialDiagnosisResponse DTOs |
| `DiagnosisRepository` | Repository | Persists and retrieves diagnosis records for audit/learning |

---

### Module 2 — Patient Management

#### What it does

CRUD for patient records including demographics, medical history, allergies, and chronic conditions. Designed to work offline-first with sync capability.

#### Key Classes

| Class | Type | Key Responsibility |
|---|---|---|
| `PatientController` | Controller | `GET/POST/PUT /api/v1/patients` — full patient lifecycle |
| `PatientService` | Service | Business rules: duplicate check, record validation, history merge |
| `PatientRepository` | Repository | JPA queries: findByNationalId, findByClinic, search by name |
| `Patient` | Entity | Core patient entity with demographics and medical baseline |

---

### Module 3 — Consultation Session

#### What it does

Manages the lifecycle of a clinical encounter: open, add notes/findings, generate AI guidance at each step, close with treatment plan attached.

#### Key Classes

| Class | Type | Key Responsibility |
|---|---|---|
| `ConsultationController` | Controller | Session endpoints: open, update, close, retrieve history |
| `ConsultationService` | Service | Orchestrates multi-turn AI dialogue, persists session state |
| `ConsultationRepository` | Repository | Queries: findActiveByProvider, findByPatientAndDate |
| `Consultation` | Entity | Session entity: status, timestamps, linked diagnoses, notes |
| `TreatmentPlanResponse` | DTO | Outbound: treatment steps, medications, follow-up schedule |

---

### Module 4 — Treatment Guidance & Context Awareness

#### What it does

Given a confirmed diagnosis, generates evidence-based treatment plans adapted to local medication availability and equipment constraints. The AI prompt includes a resource profile for the clinic.

#### Key Classes

| Class | Type | Key Responsibility |
|---|---|---|
| `TreatmentService` | Service | Generates context-aware treatment plans via Bedrock |
| `PromptUtil` | Util | Injects clinic resource profile into AI prompt context |
| `MedicalCalculator` | Util | Dosage calculation by weight/age/renal function |
| `Treatment` | Entity | Treatment record: drugs, dosages, duration, precautions |

---

### Module 5 — Specialist Escalation & Telemedicine

#### What it does

Packages a complete case summary (AI-generated) and initiates a telemedicine handoff or escalation referral. Enables asynchronous specialist review.

#### Key Classes

| Class | Type | Key Responsibility |
|---|---|---|
| `EscalationController` | Controller | `POST /api/v1/escalation/refer` — triggers escalation flow |
| `EscalationService` | Service | Compiles case bundle, calls TelemedicineAdapter, sends notification |
| `TelemedicineAdapter` | Integration | Interface to external telemedicine / messaging system |
| `NotificationService` | Service | SMS/email alerts to specialist and patient |

---

### Module 6 — Population Health Analytics

#### What it does

Aggregates anonymised case data to surface disease trends, treatment outcomes, and resource usage per region. Exposed as a read-only analytics API.

#### Key Classes

| Class | Type | Key Responsibility |
|---|---|---|
| `AnalyticsController` | Controller | `GET /api/v1/analytics/*` — trends, outcomes, cohort stats |
| `AnalyticsService` | Service | Aggregation logic, date-range filtering, anonymisation |
| `ConsultationRepository` | Repository | Custom JPQL for aggregated stats (reused from Module 3) |

---

## 4. Data Model (Key Entities)

All entities use `Long` primary keys, `@CreatedDate` / `@LastModifiedDate` audit fields, and soft-delete via an `active` boolean where appropriate.

| Entity | Core Fields | Relationships |
|---|---|---|
| `Provider` | id, name, role, clinicName, region, email | OneToMany → Consultations |
| `Patient` | id, nationalId, dob, gender, bloodGroup, allergies | OneToMany → Consultations |
| `Consultation` | id, status, chiefComplaint, vitals (JSON), openedAt | ManyToOne → Patient, Provider; OneToMany → Diagnoses |
| `Diagnosis` | id, conditionName, confidenceScore, reasoning, source | ManyToOne → Consultation; OneToMany → Treatments |
| `Treatment` | id, type, drugName, dosage, duration, instructions | ManyToOne → Diagnosis |

> **Note:** Vitals and lab results are stored as JSONB columns (PostgreSQL) for flexibility across diverse clinical contexts without schema migrations for each new data point.

---

## 5. REST API Contract

All endpoints are versioned under `/api/v1`. JSON in, JSON out. Standard HTTP status codes. Errors return `{ code, message, timestamp }`.

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/v1/diagnostic/analyze` | Run AI diagnostic analysis on patient data | Bearer |
| `POST` | `/api/v1/diagnostic/treatment` | Generate context-aware treatment plan | Bearer |
| `GET` | `/api/v1/patients` | List patients (paginated, filterable) | Bearer |
| `POST` | `/api/v1/patients` | Register new patient | Bearer |
| `GET` | `/api/v1/patients/{id}` | Get patient record | Bearer |
| `PUT` | `/api/v1/patients/{id}` | Update patient record | Bearer |
| `POST` | `/api/v1/consultations` | Open new consultation session | Bearer |
| `PUT` | `/api/v1/consultations/{id}` | Update consultation (add findings/labs) | Bearer |
| `POST` | `/api/v1/consultations/{id}/close` | Close consultation, attach treatment plan | Bearer |
| `GET` | `/api/v1/consultations/{id}/history` | Full consultation timeline | Bearer |
| `POST` | `/api/v1/escalation/refer` | Package case + escalate to specialist | Bearer |
| `GET` | `/api/v1/analytics/trends` | Disease trend data by region/date | Bearer |
| `GET` | `/api/v1/analytics/outcomes` | Treatment outcome statistics | Bearer |
| `POST` | `/api/v1/auth/register` | Register provider account | Public |
| `POST` | `/api/v1/auth/login` | Authenticate, receive JWT | Public |

---

## 6. AWS Integration (Hackathon Key)

Synthesis is AWS-native. The AI reasoning engine is built on **Amazon Bedrock** with Claude as the foundation model. This is the core differentiator for the hackathon.

| AWS Service | Usage in Synthesis | Package / Class |
|---|---|---|
| Amazon Bedrock | Foundation model (Claude) for all diagnostic AI reasoning | `integration.bedrock.BedrockClient` |
| Amazon RDS (PostgreSQL) | Production database for all clinical data | Configured in `application.yaml` |
| AWS Secrets Manager | Database credentials, Bedrock API keys | `config.SecurityConfig` |
| Amazon S3 | Storage for imaging attachments and escalation documents | `integration.storage.S3Adapter` |
| Amazon CloudWatch | Application metrics, diagnostic request logs, latency tracking | Spring Boot Actuator → CloudWatch |
| AWS Elastic Beanstalk / App Runner | Application hosting with auto-scaling | Deployment artifact (JAR) |

### Bedrock Integration Pattern

The `BedrockPromptBuilder` constructs a structured clinical prompt that includes:
1. Patient demographics and history
2. Current symptoms and vitals
3. Available diagnostic equipment
4. Local medication formulary

This context-rich prompt is what enables specialist-level reasoning.

> **Note:** `BedrockClient` wraps the AWS SDK `InvokeModelRequest`, retries on throttling, and passes the raw response to `ResponseParser` which extracts structured DTOs (differential list, confidence scores, recommended tests, treatment options).

---

## 7. Kiro Task Sequence

Follow this exact order in Kiro. Each task is a self-contained spec that Kiro can generate, review, and test independently before moving on.

| # | Kiro Task | Deliverable | Priority |
|---|---|---|---|
| 1 | Project Scaffold | `application.yaml`, `pom.xml` dependencies, `SynthesisApplication` | Critical |
| 2 | Database Config | DataSource config, JPA settings, PostgreSQL dialect | Critical |
| 3 | Domain Entities | `Patient`, `Provider`, `Consultation`, `Diagnosis`, `Treatment` @Entity | Critical |
| 4 | Repository Layer | 5 JPA Repository interfaces + custom queries | Critical |
| 5 | Exception Handling | `GlobalExceptionHandler` + custom exception classes | Critical |
| 6 | Auth Module | JWT-based auth, `SecurityConfig`, Provider registration/login | Critical |
| 7 | Bedrock Integration | `BedrockClient`, `BedrockPromptBuilder`, `ResponseParser` | Critical |
| 8 | Patient Service + Controller | Full CRUD for patients with validation | High |
| 9 | Consultation Service + Controller | Session lifecycle management | High |
| 10 | Diagnostic Service + Controller | AI analysis endpoint wired to Bedrock | High |
| 11 | Treatment Service | Context-aware plan generation via Bedrock | High |
| 12 | Escalation Module | Escalation service + telemedicine adapter stub | Medium |
| 13 | Analytics Module | Trends and outcomes endpoints | Medium |
| 14 | Utility Classes | `MedicalCalculator`, `DateUtil`, `PromptUtil` | Medium |
| 15 | Unit Tests | Service-layer tests with Mockito for modules 7–11 | High |
| 16 | Integration Tests | `@SpringBootTest` for critical API flows | High |
| 17 | Seed Data / Demo Script | SQL seed: 3 providers, 10 patients, sample consultations | Medium |

> **Kiro Tip:** When generating each task, explicitly reference the package path (e.g. `com.asakaa.synthesis.service.DiagnosticService`) so Kiro places files in the correct location without ambiguity.

---

## 8. Configuration Guide

Use `application.yaml` as the base. Override per environment with `application-{profile}.yaml`. Never hardcode secrets — use environment variables or AWS Secrets Manager references.

### application.yaml Structure

```yaml
spring:
  application:
    name: synthesis
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/synthesis}
    username: ${DB_USER:synthesis_user}
    password: ${DB_PASSWORD:changeme}
  jpa:
    hibernate.ddl-auto: validate        # use Flyway/Liquibase in prod
    show-sql: false
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect

aws:
  bedrock:
    region: ${AWS_REGION:us-east-1}
    model-id: ${BEDROCK_MODEL_ID:anthropic.claude-3-sonnet-20240229-v1:0}
  s3:
    bucket: ${S3_BUCKET:synthesis-attachments}

security:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000             # 24 hours

synthesis:
  ai:
    max-tokens: 2048
    temperature: 0.2                    # low temp for clinical accuracy
  escalation:
    notification-email: ${ESCALATION_EMAIL}
```

### Maven Dependencies to Add

| Dependency | Group / Artifact | Purpose |
|---|---|---|
| Spring Security | `spring-boot-starter-security` | JWT authentication |
| JJWT | `io.jsonwebtoken:jjwt-api + impl + jackson` | JWT token generation/validation |
| AWS SDK Bedrock | `software.amazon.awssdk:bedrockruntime` | AI model invocation |
| AWS SDK S3 | `software.amazon.awssdk:s3` | File attachment storage |
| Flyway | `org.flywaydb:flyway-core` | Database migrations |
| Spring Actuator | `spring-boot-starter-actuator` | Health checks + metrics |
| Jackson Databind | included via `spring-web` | JSON serialisation |
| TestContainers | `org.testcontainers:postgresql` | Integration test DB |

---

## 9. Testing Strategy

Three test tiers, mirroring the main source structure under `src/test/java/com/asakaa/synthesis/`.

| Tier | Annotation | Scope | Tools |
|---|---|---|---|
| Unit | `@ExtendWith(MockitoExtension)` | Single class, mocked dependencies | JUnit 5, Mockito |
| Slice | `@WebMvcTest` / `@DataJpaTest` | Controller or Repository in isolation | MockMvc, H2/TestContainers |
| Integration | `@SpringBootTest` | Full application context + real DB | TestContainers + PostgreSQL |

### Critical Test Cases

- **`DiagnosticServiceTest`** — mock `BedrockClient`, assert `DifferentialDiagnosisResponse` is correctly parsed
- **`PatientControllerTest`** — `@WebMvcTest`: `POST /patients` returns 201, `GET /patients/{id}` returns 404 for unknown
- **`ConsultationServiceTest`** — session state transitions (`OPEN → IN_PROGRESS → CLOSED`)
- **`BedrockClientTest`** — mock AWS SDK, verify prompt is sent and response parsed without exception
- **`PatientRepositoryTest`** — `@DataJpaTest`: `findByNationalId` returns correct entity

---

## 10. Development Conventions

### Naming

| Component | Convention |
|---|---|
| Controllers | Noun + Controller — `DiagnosticController`, `PatientController` |
| Services | Noun + Service — `DiagnosticService`, `ConsultationService` |
| Repositories | Noun + Repository — `PatientRepository`, `DiagnosisRepository` |
| Entities | Plain noun, singular — `Patient`, `Consultation`, `Diagnosis` |
| DTOs (Request) | Noun + Request — `DiagnosticRequest`, `PatientRequest` |
| DTOs (Response) | Noun + Response — `DiagnosticResponse`, `TreatmentPlanResponse` |
| DB Tables | snake_case, plural — `patients`, `consultations`, `diagnoses` |
| REST paths | kebab-case, plural noun — `/api/v1/consultations/{id}` |

### Lombok Usage

- `@Data` on entities and DTOs (generates getters/setters/equals/hashCode/toString)
- `@Builder` on entities and DTOs for clean construction patterns
- `@RequiredArgsConstructor` on Service and Controller classes (inject via final fields)
- `@Slf4j` on all Service classes for consistent logging

### Service Pattern

> Services never return entities directly. Always map to DTOs before returning to the controller. Keep all business logic in the Service layer — controllers are thin routers only.

### Error Handling

All exceptions bubble up to `GlobalExceptionHandler` (`@ControllerAdvice`) which returns a consistent `ErrorResponse` DTO with an HTTP status, error code, message, and timestamp. Never return stack traces to the client.

---

## 11. Hackathon Demo Flow

The demo tells a single compelling story: a community health worker in a rural clinic uses Synthesis to handle a complex case she has never seen before.

### Recommended Demo Script

1. Provider logs in (JWT auth). Dashboard shows 3 active cases in her clinic region.
2. She opens a new consultation for a 34-year-old female: fever 5 days, cough, night sweats, weight loss.
3. She enters vitals (temp 38.4°C, SpO2 96%, HR 102) and marks that sputum microscopy is available but CT is not.
4. Synthesis calls Bedrock → returns differential: **TB (72%)**, Pneumonia (18%), Lymphoma (10%) with reasoning.
5. She confirms TB. Synthesis generates a context-aware treatment plan: first-line DOTS regimen with locally available drugs and dosing by weight.
6. She triggers Specialist Escalation — Synthesis packages the case and sends it for async specialist review.
7. Analytics dashboard shows: this clinic has seen 8 suspected TB cases this quarter — surfacing a local outbreak signal.

> **Tip:** The entire flow from patient registration to specialist escalation should complete in under 3 minutes live. Seed the database beforehand with realistic data so the analytics panel looks meaningful.

---

## 12. Quick Start Checklist

Use this as your day-one Kiro kickoff checklist:

| # | Action | Command / Note |
|---|---|---|
| 1 | Clone repo + open in Kiro | `git clone <repo>` then open in Kiro IDE |
| 2 | Set AWS credentials | `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION` |
| 3 | Spin up local PostgreSQL | `docker run -e POSTGRES_DB=synthesis -p 5432:5432 postgres:16` |
| 4 | Copy `application-local.yaml` | Set `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` |
| 5 | Ask Kiro: scaffold entity layer | *"Create JPA entities per section 4 in `com.asakaa.synthesis.domain.entity`"* |
| 6 | Ask Kiro: scaffold repositories | *"Create Spring Data repositories per section 2 package tree"* |
| 7 | Ask Kiro: scaffold Bedrock client | *"Create BedrockClient in `com.asakaa.synthesis.integration.bedrock`"* |
| 8 | Build and run | `./mvnw spring-boot:run -Dspring-boot.run.profiles=local` |
| 9 | Run tests | `./mvnw test` |
| 10 | Load seed data | `psql -d synthesis -f src/main/resources/db/seed.sql` |

---

> *Build what matters. Five billion people are waiting.*
