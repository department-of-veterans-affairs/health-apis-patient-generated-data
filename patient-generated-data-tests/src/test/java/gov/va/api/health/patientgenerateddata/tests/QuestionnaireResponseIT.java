package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.sentinel.Environment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class QuestionnaireResponseIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(Environment.LOCAL);
  }

  @Test
  void read() {
    makeRequest(null, "QuestionnaireResponse/3141", 200, log);
    makeRequest("application/json", "QuestionnaireResponse/3141", 200, log);
    makeRequest("application/fhir+json", "QuestionnaireResponse/3141", 200, log);
  }

  @Test
  void read_notFound() {
    makeRequest("application/json", "QuestionnaireResponse/55555555", 404, log);
  }
}
