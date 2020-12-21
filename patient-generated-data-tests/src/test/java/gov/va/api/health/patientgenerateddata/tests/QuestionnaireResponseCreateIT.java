package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPost;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.serializePayload;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse.Status;
import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.Test;

public class QuestionnaireResponseCreateIT {
  @Test
  public void create_invalid() {
    assumeEnvironmentIn(Environment.LOCAL);
    QuestionnaireResponse questionnaireResponse = questionnaireResponse().id("123");
    doPost(
        "QuestionnaireResponse",
        serializePayload(questionnaireResponse),
        "create invalid resource, existing id",
        400);
  }

  @Test
  public void create_valid() {
    assumeEnvironmentIn(Environment.LOCAL);
    QuestionnaireResponse questionnaireResponse = questionnaireResponse();
    doPost(
        "QuestionnaireResponse", serializePayload(questionnaireResponse), "create resource", 201);
  }

  private QuestionnaireResponse questionnaireResponse() {
    return QuestionnaireResponse.builder().status(Status.completed).build();
  }
}