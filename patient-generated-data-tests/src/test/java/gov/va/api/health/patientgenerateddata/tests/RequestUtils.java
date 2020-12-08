package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestUtils {
  public static ExpectedResponse makePutRequest(
      String request, Integer expectedStatus, String payload, String changes) {
    SystemDefinitions.Service svc = systemDefinition().r4();
    RequestSpecification spec =
        RestAssured.given().baseUri(svc.url()).port(svc.port()).relaxedHTTPSValidation();
    spec.header("Content-Type", "application/json");
    spec.body(payload);
    log.info(
        "Expect {} PUT '{}' is status code ({})", svc.apiPath() + request, changes, expectedStatus);
    return ExpectedResponse.of(spec.request(Method.PUT, svc.urlWithApiPath() + request))
        .logAction(logAllWithTruncatedBody(2000))
        .expect(expectedStatus);
  }

  public static ExpectedResponse makeRequest(
      String acceptHeader, String request, Integer expectedStatus) {
    SystemDefinitions.Service svc = systemDefinition().r4();
    RequestSpecification spec =
        RestAssured.given().baseUri(svc.url()).port(svc.port()).relaxedHTTPSValidation();
    log.info(
        "Expect {} with accept header ({}) is status code ({})",
        svc.apiPath() + request,
        acceptHeader,
        expectedStatus);
    if (acceptHeader != null) {
      spec = spec.accept(acceptHeader);
    }
    return ExpectedResponse.of(spec.request(Method.GET, svc.urlWithApiPath() + request))
        .logAction(logAllWithTruncatedBody(2000))
        .expect(expectedStatus);
  }

  @SneakyThrows
  public static String serializePayload(Object payload) {
    return JacksonConfig.createMapper().writeValueAsString(payload);
  }
}
