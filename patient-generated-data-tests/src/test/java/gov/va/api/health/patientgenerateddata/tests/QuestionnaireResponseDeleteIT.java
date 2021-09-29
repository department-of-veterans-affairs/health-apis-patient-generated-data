package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.Requests.ACCESS_TOKEN;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doDelete;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doPost;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doSandboxDelete;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireResponseDeleteIT {
  @BeforeAll
  static void assumeEnvironment() {
    // These tests invent new data and then remove it
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
  }

  static QuestionnaireResponse questionnaireResponse() {
    return QuestionnaireResponse.builder().status(QuestionnaireResponse.Status.completed).build();
  }

  @Test
  void archivedDelete() {
    var response = doPost("create resource", "QuestionnaireResponse", questionnaireResponse(), 201);
    String id = response.expectValid(QuestionnaireResponse.class).id();

    doDelete("archive and delete resource", "QuestionnaireResponse/" + id, ACCESS_TOKEN, 200);
    doSandboxDelete("tear down", "Questionnaire/" + id, 200);
  }
}
