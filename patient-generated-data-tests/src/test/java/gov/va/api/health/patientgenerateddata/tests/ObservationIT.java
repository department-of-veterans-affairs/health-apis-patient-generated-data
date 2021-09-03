package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.Test;

public class ObservationIT {
  @Test
  void read() {
    String id = systemDefinition().ids().observation();
    doGet(null, "Observation/" + id, 200);
    doGet("application/json", "Observation/" + id, 200);
    doGet("application/fhir+json", "Observation/" + id, 200);
  }

  @Test
  void read_notFound() {
    doGet("application/json", "Observation/5555555", 404);
  }

  @Test
  void search_csv() {
    String id = String.join(",", systemDefinition().ids().observationList());
    String query = String.format("?_id=%s", id);
    var response = doGet("application/json", "Observation" + query, 200);
    Observation.Bundle bundle = response.expectValid(Observation.Bundle.class);
    assertThat(bundle.entry()).isNotEmpty();
  }

  @Test
  void search_lastUpdated() {
    // LOCAL only since we cannot restrict the search to the static token patient
    assumeEnvironmentIn(Environment.LOCAL);
    var lastUpdated = systemDefinition().ids().lastUpdated();
    var response = doGet("application/json", "Observation?_lastUpdated=" + lastUpdated, 200);
    Observation.Bundle bundle = response.expectValid(Observation.Bundle.class);
    assertThat(bundle.entry()).isNotEmpty();
  }
}
