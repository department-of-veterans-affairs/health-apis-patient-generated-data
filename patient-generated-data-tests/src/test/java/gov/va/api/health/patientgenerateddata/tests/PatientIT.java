package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PatientIT {
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
    doGet(null, "Patient/1000000", 200);
    doGet(null, "Patient/2000000", 200);
    doGet(null, "Patient/3000000", 200);
    doGet("application/json", "Patient/1000000", 200);
    doGet("application/fhir+json", "Patient/1000000", 200);
  }

  @Test
  void read_notFound() {
    doGet("application/json", "Patient/5555555", 404);
  }
}
