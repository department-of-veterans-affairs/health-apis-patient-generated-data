package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.sentinel.Environment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class OpenApiIT {
  @Test
  void openApi() {
    assumeEnvironmentIn(Environment.LOCAL, Environment.QA);
    makeRequest("application/json", "openapi.json", 200);
    makeRequest(null, "openapi.json", 200);
  }

  @Test
  void openApiExp() {
    assumeEnvironmentIn(Environment.STAGING_LAB, Environment.STAGING);
    try {
      makeRequest("application/json", "openapi.json", 200);
      makeRequest(null, "openapi.json", 200);
    } catch (Throwable tr) {
      log.warn("exception", tr);
    }
  }
}
