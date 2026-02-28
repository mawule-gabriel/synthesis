package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.PatientHistoryTimeline;
import com.asakaa.synthesis.domain.dto.PatientHistoryTimelineEvent;
import com.asakaa.synthesis.domain.entity.*;
import com.asakaa.synthesis.repository.ConsultationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosticServiceHistoryTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @InjectMocks
    private DiagnosticService diagnosticService;

    @Test
    void buildPatientHistoryTimeline_returnsChronologicalEvents() {
        Patient patient = new Patient();
        patient.setId(1L);

        LocalDateTime consultDate1 = LocalDateTime.of(2025, 10, 1, 9, 0);
        LocalDateTime consultDate2 = LocalDateTime.of(2025, 12, 15, 14, 30);

        Consultation consultation1 = buildConsultation(1L, patient, "Headache", consultDate1);
        Consultation consultation2 = buildConsultation(2L, patient, "Follow-up", consultDate2);

        when(consultationRepository.findByPatientIdAndOpenedAtAfterOrderByOpenedAtAsc(eq(1L), any()))
                .thenReturn(List.of(consultation1, consultation2));

        PatientHistoryTimeline timeline = diagnosticService.buildPatientHistoryTimeline(patient);

        assertNotNull(timeline);
        assertEquals("1", timeline.getPatientId());
        assertEquals(2, timeline.getTotalConsultations());
        assertFalse(timeline.getEvents().isEmpty());

        // Verify chronological order
        List<PatientHistoryTimelineEvent> events = timeline.getEvents();
        for (int i = 1; i < events.size(); i++) {
            assertTrue(
                    !events.get(i).getEventDate().isBefore(events.get(i - 1).getEventDate()),
                    "Events should be in chronological order"
            );
        }
    }

    @Test
    void buildPatientHistoryTimeline_includesAllEventTypes() {
        Patient patient = new Patient();
        patient.setId(2L);

        LocalDateTime consultDate = LocalDateTime.of(2025, 11, 1, 10, 0);
        Consultation consultation = buildConsultation(10L, patient, "Chest pain", consultDate);

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(100L);
        diagnosis.setConditionName("Angina");
        diagnosis.setConfidenceScore(new BigDecimal("0.85"));
        diagnosis.setReasoning("Based on symptoms and ECG");
        diagnosis.setConsultation(consultation);
        consultation.setDiagnoses(List.of(diagnosis));

        LabResult lab = new LabResult();
        lab.setId(200L);
        lab.setTestName("Troponin");
        lab.setNumericValue(new BigDecimal("0.04"));
        lab.setUnit("ng/mL");
        lab.setIsAbnormal(false);
        lab.setReferenceRange("0.00-0.04");
        lab.setRecordedAt(LocalDateTime.of(2025, 11, 1, 11, 0));
        lab.setConsultation(consultation);
        consultation.setLabResults(List.of(lab));

        ImageAnalysis imaging = new ImageAnalysis();
        imaging.setId(300L);
        imaging.setDescription("Chest X-ray");
        imaging.setFindings(List.of("No acute findings", "Normal cardiac silhouette"));
        imaging.setAnalyzedAt(LocalDateTime.of(2025, 11, 1, 12, 0));
        imaging.setConsultation(consultation);
        consultation.setImageAnalyses(List.of(imaging));

        when(consultationRepository.findByPatientIdAndOpenedAtAfterOrderByOpenedAtAsc(eq(2L), any()))
                .thenReturn(List.of(consultation));

        PatientHistoryTimeline timeline = diagnosticService.buildPatientHistoryTimeline(patient);

        assertEquals(1, timeline.getTotalConsultations());
        assertEquals(1, timeline.getTotalDiagnoses());
        assertEquals(1, timeline.getTotalLabResults());
        assertEquals(1, timeline.getTotalImagingFindings());

        List<String> eventTypes = timeline.getEvents().stream()
                .map(PatientHistoryTimelineEvent::getEventType)
                .toList();

        assertTrue(eventTypes.contains("CONSULTATION"), "Should include consultation events");
        assertTrue(eventTypes.contains("DIAGNOSIS"), "Should include diagnosis events");
        assertTrue(eventTypes.contains("LAB_RESULT"), "Should include lab result events");
        assertTrue(eventTypes.contains("IMAGING"), "Should include imaging events");

        assertEquals(4, timeline.getEvents().size(), "Should have exactly 4 events (1 of each type)");
    }

    @Test
    void buildPatientHistoryTimeline_eventsSortedChronologically() {
        Patient patient = new Patient();
        patient.setId(3L);

        // Consultation opened at 10:00, but lab recorded at 09:00 and imaging at 11:00
        LocalDateTime consultDate = LocalDateTime.of(2025, 11, 5, 10, 0);
        Consultation consultation = buildConsultation(20L, patient, "Fever", consultDate);

        LabResult lab = new LabResult();
        lab.setId(201L);
        lab.setTestName("CBC");
        lab.setNumericValue(new BigDecimal("12000"));
        lab.setUnit("cells/uL");
        lab.setIsAbnormal(true);
        lab.setReferenceRange("4500-11000");
        lab.setRecordedAt(LocalDateTime.of(2025, 11, 5, 9, 0)); // before consultation
        lab.setConsultation(consultation);
        consultation.setLabResults(List.of(lab));

        ImageAnalysis imaging = new ImageAnalysis();
        imaging.setId(301L);
        imaging.setDescription("Chest X-ray");
        imaging.setFindings(List.of("Infiltrate in right lower lobe"));
        imaging.setAnalyzedAt(LocalDateTime.of(2025, 11, 5, 11, 0)); // after consultation
        imaging.setConsultation(consultation);
        consultation.setImageAnalyses(List.of(imaging));

        when(consultationRepository.findByPatientIdAndOpenedAtAfterOrderByOpenedAtAsc(eq(3L), any()))
                .thenReturn(List.of(consultation));

        PatientHistoryTimeline timeline = diagnosticService.buildPatientHistoryTimeline(patient);

        List<PatientHistoryTimelineEvent> events = timeline.getEvents();
        assertEquals(3, events.size());

        // Lab at 09:00 should be first, consultation at 10:00 second, imaging at 11:00 third
        assertEquals("LAB_RESULT", events.get(0).getEventType());
        assertEquals("CONSULTATION", events.get(1).getEventType());
        assertEquals("IMAGING", events.get(2).getEventType());
    }

    @Test
    void buildPatientHistoryTimeline_emptyHistoryReturnsEmptyTimeline() {
        Patient patient = new Patient();
        patient.setId(4L);

        when(consultationRepository.findByPatientIdAndOpenedAtAfterOrderByOpenedAtAsc(eq(4L), any()))
                .thenReturn(Collections.emptyList());

        PatientHistoryTimeline timeline = diagnosticService.buildPatientHistoryTimeline(patient);

        assertNotNull(timeline);
        assertEquals("4", timeline.getPatientId());
        assertTrue(timeline.getEvents().isEmpty());
        assertEquals(0, timeline.getTotalConsultations());
        assertEquals(0, timeline.getTotalDiagnoses());
        assertEquals(0, timeline.getTotalLabResults());
        assertEquals(0, timeline.getTotalImagingFindings());
    }

    @Test
    void formatPatientHistoryForPrompt_emptyTimeline() {
        PatientHistoryTimeline timeline = PatientHistoryTimeline.builder()
                .patientId("5")
                .timeframePeriod("6 months")
                .events(Collections.emptyList())
                .totalConsultations(0)
                .totalDiagnoses(0)
                .totalLabResults(0)
                .totalImagingFindings(0)
                .build();

        String result = diagnosticService.formatPatientHistoryForPrompt(timeline);

        assertEquals("No consultation history available for the past 6 months.", result);
    }

    @Test
    void formatPatientHistoryForPrompt_producesExpectedOutput() {
        List<PatientHistoryTimelineEvent> events = List.of(
                PatientHistoryTimelineEvent.builder()
                        .eventDate(LocalDateTime.of(2025, 10, 1, 9, 0))
                        .eventType("CONSULTATION")
                        .description("Consultation: Headache")
                        .consultationId("1")
                        .details("Chief complaint: Headache")
                        .build(),
                PatientHistoryTimelineEvent.builder()
                        .eventDate(LocalDateTime.of(2025, 10, 1, 10, 0))
                        .eventType("DIAGNOSIS")
                        .description("Diagnosis: Migraine (confidence: 0.90)")
                        .consultationId("1")
                        .details("Reasoning: Recurring unilateral headache with aura")
                        .build()
        );

        PatientHistoryTimeline timeline = PatientHistoryTimeline.builder()
                .patientId("5")
                .timeframePeriod("6 months")
                .events(events)
                .totalConsultations(1)
                .totalDiagnoses(1)
                .totalLabResults(0)
                .totalImagingFindings(0)
                .build();

        String result = diagnosticService.formatPatientHistoryForPrompt(timeline);

        assertNotNull(result);
        assertTrue(result.contains("PATIENT HISTORY"), "Should contain header");
        assertTrue(result.contains("1 consultation(s)"), "Should contain consultation count");
        assertTrue(result.contains("1 diagnosis/diagnoses"), "Should contain diagnosis count");
        assertTrue(result.contains("Consultation: Headache"), "Should contain consultation event");
        assertTrue(result.contains("Diagnosis: Migraine"), "Should contain diagnosis event");
        assertTrue(result.contains("2025-10-01"), "Should contain formatted date");
    }

    @Test
    void buildPatientHistoryTimeline_queriesSixMonthWindow() {
        Patient patient = new Patient();
        patient.setId(1L);
        when(consultationRepository.findByPatientIdAndOpenedAtAfterOrderByOpenedAtAsc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of());

        diagnosticService.buildPatientHistoryTimeline(patient);

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(consultationRepository).findByPatientIdAndOpenedAtAfterOrderByOpenedAtAsc(eq(1L), captor.capture());
        LocalDateTime captured = captor.getValue();
        LocalDateTime expectedApprox = LocalDateTime.now().minusMonths(6);
        assertTrue(captured.isAfter(expectedApprox.minusMinutes(1)));
        assertTrue(captured.isBefore(expectedApprox.plusMinutes(1)));
    }

    private Consultation buildConsultation(Long id, Patient patient, String complaint, LocalDateTime openedAt) {
        Consultation consultation = new Consultation();
        consultation.setId(id);
        consultation.setPatient(patient);
        consultation.setChiefComplaint(complaint);
        consultation.setOpenedAt(openedAt);
        consultation.setStatus(ConsultationStatus.CLOSED);
        consultation.setDiagnoses(new ArrayList<>());
        consultation.setLabResults(new ArrayList<>());
        consultation.setImageAnalyses(new ArrayList<>());
        return consultation;
    }
}
