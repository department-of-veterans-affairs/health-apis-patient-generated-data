package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ObservationIT {
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
    makeRequest(null, "Observation/ekg", 200);
    makeRequest("application/json", "Observation/ekg", 200);
    makeRequest("application/fhir+json", "Observation/ekg", 200);
  }

  @Test
  void read_notFound() {
    makeRequest("application/json", "Observation/5555555", 404);
  }
}
