package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doDelete;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPost;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.sentinel.Environment;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SandboxDeleteIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
  }

  private static Observation observation() {
    return Observation.builder()
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

  private static Questionnaire questionnaire() {
    return Questionnaire.builder()
        .title("x")
        .status(Questionnaire.PublicationStatus.active)
        .build();
  }

  private static QuestionnaireResponse questionnaireResponse() {
    return QuestionnaireResponse.builder().status(QuestionnaireResponse.Status.completed).build();
  }

  @Test
  void deleteObservation() {
    Observation observation = observation();
    var response = doPost("Observation", observation, "create resource", 201);
    assertThat(response).isNotNull();
    Observation observationWritten = response.expectValid(Observation.class);
    String id = observationWritten.id();
    doGet("application/json", "Observation/" + id, 200);
    String query = String.format("Observation/%s", id);
    doDelete(query, "delete resource", 200);
    doGet("application/json", "Observation/" + id, 404);
  }

  @Test
  void deleteQuestionnaire() {
    Questionnaire questionnaire = questionnaire();
    var response = doPost("Questionnaire", questionnaire, "create resource", 201);
    assertThat(response).isNotNull();
    Questionnaire questionnaireWritten = response.expectValid(Questionnaire.class);
    String id = questionnaireWritten.id();
    doGet("application/json", "Questionnaire/" + id, 200);
    String query = String.format("Questionnaire/%s", id);
    doDelete(query, "delete resource", 200);
    doGet("application/json", "Questionnaire/" + id, 404);
  }

  @Test
  void deleteQuestionnaireResponse() {
    QuestionnaireResponse questionnaireResponse = questionnaireResponse();
    var response = doPost("QuestionnaireResponse", questionnaireResponse, "create resource", 201);
    QuestionnaireResponse questionnaireResponseWritten =
        response.expectValid(QuestionnaireResponse.class);
    String id = questionnaireResponseWritten.id();
    doGet("application/json", "QuestionnaireResponse/" + id, 200);
    String query = String.format("QuestionnaireResponse/%s", id);
    doDelete(query, "delete resource", 200);
    doGet("application/json", "QuestionnaireResponse/" + id, 404);
  }
}
