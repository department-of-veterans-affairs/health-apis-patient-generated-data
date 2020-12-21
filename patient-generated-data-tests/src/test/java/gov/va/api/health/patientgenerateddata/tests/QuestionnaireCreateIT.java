package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPost;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.serializePayload;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.Questionnaire.PublicationStatus;
import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.Test;

public class QuestionnaireCreateIT {
  @Test
  public void create_invalid() {
    assumeEnvironmentIn(Environment.LOCAL);
    Questionnaire questionnaire = questionnaire().id("123");
    doPost(
        "Questionnaire",
        serializePayload(questionnaire),
        "create invalid resource, existing id",
        400);
  }

  @Test
  public void create_valid() {
    assumeEnvironmentIn(Environment.LOCAL);
    Questionnaire questionnaire = questionnaire();
    doPost("Questionnaire", serializePayload(questionnaire), "create resource", 201);
  }

  private Questionnaire questionnaire() {
    return Questionnaire.builder().title("x").status(PublicationStatus.active).build();
  }
}
