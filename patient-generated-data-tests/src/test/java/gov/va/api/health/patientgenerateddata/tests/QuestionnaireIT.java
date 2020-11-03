package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;

import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class QuestionnaireIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(Environment.LOCAL);
  }

  static ExpectedResponse makeRequest(String acceptHeader, String request, Integer expectedStatus) {
    SystemDefinitions.Service svc = systemDefinition().r4();
    log.info(
        "Expect {} with accept header ({}) is status code ({})",
        svc.apiPath() + request,
        acceptHeader,
        expectedStatus);
    RequestSpecification spec =
        RestAssured.given().baseUri(svc.url()).port(svc.port()).relaxedHTTPSValidation();
    if (acceptHeader != null) {
      spec = spec.accept(acceptHeader);
    }
    return ExpectedResponse.of(spec.request(Method.GET, svc.urlWithApiPath() + request))
        .logAction(logAllWithTruncatedBody(2000))
        .expect(expectedStatus);
  }

  @Test
  void read() {
    makeRequest(null, "Questionnaire/c18", 200);
    makeRequest("application/json", "Questionnaire/c18", 200);
    makeRequest("application/fhir+json", "Questionnaire/c18", 200);
  }

  @Test
  void read_notFound() {
    makeRequest("application/json", "Questionnaire/5555555", 404);
  }
}
