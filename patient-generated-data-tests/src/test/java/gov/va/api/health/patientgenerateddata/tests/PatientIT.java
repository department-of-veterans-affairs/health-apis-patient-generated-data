package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

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
    String id = systemDefinition().ids().patient();
    doGet(null, "Patient/" + id, 200);
    doGet("application/json", "Patient/" + id, 200);
    doGet("application/fhir+json", "Patient/" + id, 200);
  }

  @Test
  void read_notMe() {
    // Kong required
    assumeEnvironmentNotIn(Environment.LOCAL);
    String id = systemDefinition().ids().patientNotMe();
    doGet("application/json", "Patient/" + id, 403);
    // does not exist
    doGet("application/json", "Patient/5555555", 403);
  }
}
