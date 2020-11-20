package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.sentinel.Environment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
public class PatientIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(Environment.LOCAL);
  }

  @Test
  @Disabled
  void read() {
    makeRequest(null, "Patient/1000000", 200, log);
    makeRequest(null, "Patient/2000000", 200, log);
    makeRequest(null, "Patient/3000000", 200, log);
    makeRequest("application/json", "Patient/1000000", 200, log);
    makeRequest("application/fhir+json", "Patient/1000000", 200, log);
  }

  @Test
  @Disabled
  void read_notFound() {
    makeRequest("application/json", "Patient/5555555", 404, log);
  }
}
