package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doDelete;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPost;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

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
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.LAB, Environment.STAGING_LAB);
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
    assumeEnvironmentIn(Environment.STAGING, Environment.PROD);
    doDelete("Observation/12345", "delete resource", 200);
  }

  @Test
  void deleteObservation() {
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
    Observation observation = observation();
    var response = doPost("Observation", observation, "create resource", 201);
    Observation observationWritten = response.expectValid(Observation.class);
    String id = observationWritten.id();
    doGet("application/json", "Observation/" + id, 200);
    doDelete("Observation/" + id, "delete resource", 200);
    doGet("application/json", "Observation/" + id, 404);
  }

  @Test
  void deleteQuestionnaire() {
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
    Questionnaire questionnaire = questionnaire();
    var response = doPost("Questionnaire", questionnaire, "create resource", 201);
    Questionnaire questionnaireWritten = response.expectValid(Questionnaire.class);
    String id = questionnaireWritten.id();
    doGet("application/json", "Questionnaire/" + id, 200);
    doDelete("Questionnaire/" + id, "delete resource", 200);
    doGet("application/json", "Questionnaire/" + id, 404);
  }

  @Test
  void deleteQuestionnaireResponse() {
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
    QuestionnaireResponse questionnaireResponse = questionnaireResponse();
    var response = doPost("QuestionnaireResponse", questionnaireResponse, "create resource", 201);
    QuestionnaireResponse questionnaireResponseWritten =
        response.expectValid(QuestionnaireResponse.class);
    String id = questionnaireResponseWritten.id();
    doGet("application/json", "QuestionnaireResponse/" + id, 200);
    doDelete("QuestionnaireResponse/" + id, "delete resource", 200);
    doGet("application/json", "QuestionnaireResponse/" + id, 404);
  }
}
