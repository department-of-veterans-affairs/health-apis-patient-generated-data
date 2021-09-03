package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doDelete;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPost;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireCreateIT {
  @BeforeAll
  static void assumeEnvironment() {
    // These tests invent new data and then remove it
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
  }

  static Questionnaire questionnaire() {
    return Questionnaire.builder()
        .title("x")
        .status(Questionnaire.PublicationStatus.active)
        .build();
  }

  @Test
  void create_invalid() {
    doPost(
        "Questionnaire", questionnaire().id("123"), "create invalid resource (existing ID)", 400);
  }

  @Test
  void create_valid() {
    var response = doPost("Questionnaire", questionnaire(), "create resource", 201);
    String id = response.expectValid(Questionnaire.class).id();
    doDelete("Questionnaire/" + id, "tear down", 200);
  }
}
