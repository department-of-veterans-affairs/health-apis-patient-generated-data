package gov.va.api.health.patientgenerateddata.tests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class SmokeTestIT {
  @Test
  void health() {
    String url = System.getProperty("patient-generated-data.url");
	assertThat(url).isNotNull();
    ExpectedResponse.of(
            RestAssured.given()
                .baseUri(url)
                .relaxedHTTPSValidation()
                .request(Method.GET, url + "/actuator/health"))
        .expect(200);
  }
}
