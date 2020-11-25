package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;

import org.junit.jupiter.api.Test;

public class OpenApiIT {
  // value = {"", ""},
  //  @Test
  //  void openApi() {
  //    Service svc = systemDefinition().r4();
  //    String request = svc.urlWithApiPath() + "actuator/health";
  //    ExpectedResponse.of(
  //            RestAssured.given()
  //                .baseUri(svc.url())
  //                .port(svc.port())
  //                .relaxedHTTPSValidation()
  //                .request(Method.GET, request))
  //        .expect(200);
  //  }

  @Test
  void openApi() {
    makeRequest(null, "", 200);
    makeRequest(null, "openapi.json", 200);
  }
}
