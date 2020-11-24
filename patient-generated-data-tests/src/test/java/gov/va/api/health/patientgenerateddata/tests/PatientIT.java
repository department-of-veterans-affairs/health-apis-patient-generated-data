package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PatientIT {
  @BeforeAll
  static void assumeEnvironment() {
	  assumeEnvironmentIn(Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
  }

  @Test
  void read() {
    makeRequest(null, "Patient/1000000", 200);
    makeRequest(null, "Patient/2000000", 200);
    makeRequest(null, "Patient/3000000", 200);
    makeRequest("application/json", "Patient/1000000", 200);
    makeRequest("application/fhir+json", "Patient/1000000", 200);
  }

  @Test
  void read_notFound() {
    makeRequest("application/json", "Patient/5555555", 404);
  }
}
