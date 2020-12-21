package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(
        Environment.LOCAL,
        Environment.QA,
        Environment.STAGING,
        Environment.STAGING_LAB,
        Environment.LAB);
  }

  @Test
  void read() {
    assumeEnvironmentNotIn(Environment.LAB);
    String id = systemDefinition().ids().questionnaire();
    doGet(null, "Questionnaire/" + id, 200);
    doGet("application/json", "Questionnaire/" + id, 200);
    doGet("application/fhir+json", "Questionnaire/" + id, 200);
  }

  @Test
  void read_notFound() {
    doGet("application/json", "Questionnaire/5555555", 404);
  }
}
