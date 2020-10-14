package gov.va.api.health.patientgenerateddata.tests;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class SmokeTestIT {
  @Test
  void health() {
    String url = System.getProperty("patient-generated-data.url");
    assertThat(url).isNotNull();
    String request = url + "/actuator/health";
    log.info(request);
    ExpectedResponse.of(
            RestAssured.given().baseUri(url).relaxedHTTPSValidation().request(Method.GET, request))
        .expect(200);
  }
}
