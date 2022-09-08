package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.Requests.doGet;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doPost;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doSandboxDelete;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.sentinel.Environment;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SandboxDeleteIT {
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
        .subject(Reference.builder().reference("Patient/1011537977V693883").build())
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
  void deleteDisallowed() {
    assumeEnvironmentIn(Environment.PROD);
    doSandboxDelete("delete disallowed", "Observation/5555555", 404);
  }

  @Test
  void deleteObservation() {
    assumeEnvironmentNotIn(Environment.PROD);
    Observation observation = observation();
    var response = doPost("create resource", "Observation", observation, 201);
    Observation observationWritten = response.expectValid(Observation.class);
    String id = observationWritten.id();
    doGet("application/json", "Observation/" + id, 200);
    doSandboxDelete("delete resource", "Observation/" + id, 200);
    doGet("application/json", "Observation/" + id, 404);
  }

  @Test
  void deleteQuestionnaire() {
    assumeEnvironmentNotIn(Environment.PROD);
    Questionnaire questionnaire = questionnaire();
    var response = doPost("create resource", "Questionnaire", questionnaire, 201);
    Questionnaire questionnaireWritten = response.expectValid(Questionnaire.class);
    String id = questionnaireWritten.id();
    doGet("application/json", "Questionnaire/" + id, 200);
    doSandboxDelete("delete resource", "Questionnaire/" + id, 200);
    doGet("application/json", "Questionnaire/" + id, 404);
  }

  @Test
  void deleteQuestionnaireResponse() {
    assumeEnvironmentNotIn(Environment.PROD);
    QuestionnaireResponse questionnaireResponse = questionnaireResponse();
    var response = doPost("create resource", "QuestionnaireResponse", questionnaireResponse, 201);
    QuestionnaireResponse questionnaireResponseWritten =
        response.expectValid(QuestionnaireResponse.class);
    String id = questionnaireResponseWritten.id();
    doGet("application/json", "QuestionnaireResponse/" + id, 200);
    doSandboxDelete("delete resource", "QuestionnaireResponse/" + id, 200);
    doGet("application/json", "QuestionnaireResponse/" + id, 404);
  }
}
