package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPost;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireResponseCreateIT {
  @BeforeAll
  static void assumeEnvironment() {
    // These tests invent data that will not be cleaned up
    // To avoid polluting the database, they should only run locally
    assumeEnvironmentIn(Environment.LOCAL);
  }

  private static QuestionnaireResponse questionnaireResponse() {
    return QuestionnaireResponse.builder().status(QuestionnaireResponse.Status.completed).build();
  }

  @Test
  void create_invalid() {
    QuestionnaireResponse questionnaireResponse = questionnaireResponse().id("123");
    doPost(
        "QuestionnaireResponse",
        questionnaireResponse,
        "create invalid resource, existing id",
        400);
  }

  @Test
  void create_valid() {
    QuestionnaireResponse questionnaireResponse = questionnaireResponse();
    doPost("QuestionnaireResponse", questionnaireResponse, "create resource", 201);
  }
}
