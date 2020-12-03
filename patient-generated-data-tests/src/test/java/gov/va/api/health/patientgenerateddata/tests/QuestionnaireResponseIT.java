package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireResponseIT {
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
    makeRequest(null, "QuestionnaireResponse/3141", 200);
    makeRequest("application/json", "QuestionnaireResponse/3141", 200);
    makeRequest("application/fhir+json", "QuestionnaireResponse/3141", 200);
  }

  @Test
  void read_notFound() {
    makeRequest("application/json", "QuestionnaireResponse/55555555", 404);
  }
}
