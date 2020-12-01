package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gov.va.api.health.sentinel.Environment;

public class OpenApiIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
  }

  @Test
  void openApi_json() {
    makeRequest("application/json", "openapi.json", 200);
  }

  @Test
  void openApi_null() {
    makeRequest(null, "openapi.json", 200);
  }
}
