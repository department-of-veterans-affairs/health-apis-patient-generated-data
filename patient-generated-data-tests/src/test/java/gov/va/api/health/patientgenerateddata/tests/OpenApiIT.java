package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gov.va.api.health.sentinel.Environment;

public class OpenApiIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(Environment.LOCAL, Environment.QA);
  }

  @Test
  void openApi() {
    makeRequest("application/json", "openapi.json", 200);
    makeRequest(null, "openapi.json", 200);
  }
}
