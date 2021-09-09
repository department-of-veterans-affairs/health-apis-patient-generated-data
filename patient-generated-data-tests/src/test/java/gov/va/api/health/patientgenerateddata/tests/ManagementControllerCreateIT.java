package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.CLIENT_KEY_DEFAULT;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doDelete;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalPost;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.sentinel.Environment;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ManagementControllerCreateIT {
  private static final String CLIENT_KEY = System.getProperty("client-key", CLIENT_KEY_DEFAULT);

  private static final String TEST_ID = "it" + System.currentTimeMillis();

  static Observation observation() {
    return Observation.builder()
        .id(TEST_ID)
        .status(Observation.ObservationStatus.unknown)
        .effectiveDateTime("2020-01-01T01:00:00Z")
        .category(
            List.of(
                CodeableConcept.builder()
                    .coding(
                        List.of(
                            Coding.builder()
                                .system(
                                    "http://terminology.hl7.org/CodeSystem/observation-category")
                                .code("laboratory")
                                .build()))
                    .text("laboratory")
                    .build()))
        .code(CodeableConcept.builder().text("code").build())
        .subject(Reference.builder().reference("Patient/1").build())
        .build();
  }

  static Questionnaire questionnaire() {
    return Questionnaire.builder()
        .id(TEST_ID)
        .title("x")
        .status(Questionnaire.PublicationStatus.active)
        .build();
  }

  static QuestionnaireResponse questionnaireResponse() {
    return QuestionnaireResponse.builder()
        .id(TEST_ID)
        .status(QuestionnaireResponse.Status.completed)
        .build();
  }

  @BeforeAll
  static void setUp() {
    // These tests invent new data and then remove it
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
  }

  @AfterAll
  static void tearDown() {
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
    doDelete("tear down", "Observation/" + TEST_ID, 200);
    doDelete("tear down", "Questionnaire/" + TEST_ID, 200);
    doDelete("tear down", "QuestionnaireResponse/" + TEST_ID, 200);
  }

  @Test
  void create_observation_knownId() {
    doInternalPost("create resource with known ID", "Observation", observation(), CLIENT_KEY, 201);
  }

  @Test
  void create_questionnaireResponse_knownId() {
    doInternalPost(
        "create resource with known ID",
        "QuestionnaireResponse",
        questionnaireResponse(),
        CLIENT_KEY,
        201);
  }

  @Test
  void create_questionnaire_knownId() {
    doInternalPost(
        "create resource with known ID", "Questionnaire", questionnaire(), CLIENT_KEY, 201);
  }
}
