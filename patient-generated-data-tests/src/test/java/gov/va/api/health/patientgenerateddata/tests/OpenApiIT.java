package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.sentinel.Environment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class OpenApiIT {
	private static void assumeEnvironment() {
		assumeEnvironmentIn(
	        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
	}
	
  @Test
  void openApi_json() {
    assumeEnvironment();
    makeRequest("application/json", "openapi.json", 200);
  }

  @Test
  void openApi_lab() {
    assumeEnvironmentIn(Environment.LAB);
    try {
      makeRequest("application/json", "openapi.json", 200);
    } catch (Throwable tr) {
      log.warn("exception", tr);
    }
  }

  @Test
  void openApi_null() {
    assumeEnvironment();
    makeRequest(null, "openapi.json", 200);
  }
}
