package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Observation;
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
    String id = systemDefinition().ids().observation();
    doGet(null, "Observation/" + id, 200);
    doGet("application/json", "Observation/" + id, 200);
    doGet("application/fhir+json", "Observation/" + id, 200);
  }

  @Test
  void read_csv() {
    String id = systemDefinition().ids().observationList();
    String query = String.format("?id=%s", id);
    var response = doGet("application/json", "Observation" + query, 200);
    Observation.Bundle bundle = response.expectValid(Observation.Bundle.class);
    assertThat(bundle.entry()).hasSize(2);
  }

  @Test
  void read_notFound() {
    doGet("application/json", "Observation/5555555", 404);
  }
}
