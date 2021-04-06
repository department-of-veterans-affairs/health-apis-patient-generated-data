package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalPost;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.CLIENT_KEY_DEFAULT;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

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

public class ManagementControllerCreateIT {
  private static final String CLIENT_KEY = System.getProperty("client-key", CLIENT_KEY_DEFAULT);

  @BeforeAll
  static void assumeEnvironment() {
    // These tests invent data that will not be cleaned up
    // To avoid polluting the database, they should only run locally
    assumeEnvironmentIn(Environment.LOCAL);
  }

  private Observation _observation(String id) {
    return Observation.builder()
        .id(id)
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

  private Questionnaire _questionnaire(String id) {
    return Questionnaire.builder()
        .id(id)
        .title("x")
        .status(Questionnaire.PublicationStatus.active)
        .build();
  }

  private QuestionnaireResponse _questionnaireResponse(String id) {
    return QuestionnaireResponse.builder()
        .id(id)
        .status(QuestionnaireResponse.Status.completed)
        .build();
  }

  @Test
  void create_observation() {
    var observation = _observation("it" + System.currentTimeMillis());
    doInternalPost("Observation", observation, "create resource", 201, CLIENT_KEY);
  }

  @Test
  void create_questionnaire() {
    var questionnaire = _questionnaire("it" + System.currentTimeMillis());
    doInternalPost("Questionnaire", questionnaire, "create resource", 201, CLIENT_KEY);
  }

  @Test
  void create_questionnaire_response() {
    var questionnaireResponse = _questionnaireResponse("it" + System.currentTimeMillis());
    doInternalPost(
        "QuestionnaireResponse", questionnaireResponse, "create resource", 201, CLIENT_KEY);
  }
}
