package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;

import org.junit.jupiter.api.Test;

public class OpenApiIT {
  @Test
  void openApi() {
    makeRequest(null, "", 200);
    makeRequest(null, "openapi.json", 200);
  }
}
