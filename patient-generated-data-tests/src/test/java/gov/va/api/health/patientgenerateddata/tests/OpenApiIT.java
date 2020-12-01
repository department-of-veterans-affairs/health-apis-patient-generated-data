package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import org.junit.jupiter.api.Test;

public class OpenApiIT {
  @Test
  void openApi_json() {
    makeRequest("application/json", "openapi.json", 200);
  }

  @Test
  void openApi_null() {
    makeRequest(null, "openapi.json", 200);
  }
}
