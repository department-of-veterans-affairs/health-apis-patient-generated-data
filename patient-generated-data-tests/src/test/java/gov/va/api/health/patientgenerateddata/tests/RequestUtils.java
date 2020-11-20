package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;

import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
public class RequestUtils {

  public static ExpectedResponse makeRequest(
      String acceptHeader, String request, Integer expectedStatus) {
    return makeRequest(acceptHeader, request, expectedStatus, log);
  }

  public static ExpectedResponse makeRequest(
      String acceptHeader, String request, Integer expectedStatus, Logger logger) {
    SystemDefinitions.Service svc = systemDefinition().r4();
    RequestSpecification spec =
        RestAssured.given().baseUri(svc.url()).port(svc.port()).relaxedHTTPSValidation();
    logger.info(
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
}
