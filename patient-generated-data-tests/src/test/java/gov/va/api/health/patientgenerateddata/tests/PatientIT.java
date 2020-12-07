package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.sentinel.Environment;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
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
    makeRequest(null, "Patient/1011537977V693883", 200);
    makeRequest("application/json", "Patient/1011537977V693883", 200);
    makeRequest("application/fhir+json", "Patient/1011537977V693883", 200);
  }

  @Test
  void read_notMe() {
    assumeEnvironmentNotIn(Environment.LAB);

    // Kong required
    assumeEnvironmentNotIn(Environment.LOCAL);
    makeRequest("application/json", "Patient/1000000", 403);
    // does not exist
    makeRequest("application/json", "Patient/5555555", 403);
  }

  @Test
  void read_notMe_lab() {
    assumeEnvironmentIn(Environment.LAB);
    try {
      makeRequest("application/json", "Patient/1000000", 403);
      makeRequest("application/json", "Patient/5555555", 403);
    } catch (Throwable tr) {
      log.warn("exception", tr);
    }
  }
}
