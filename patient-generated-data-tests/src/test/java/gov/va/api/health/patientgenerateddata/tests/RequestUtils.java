package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestUtils {
  public static final ObjectMapper MAPPER =
      JacksonConfig.createMapper().registerModule(new Resource.ResourceModule());

  private static final String ACCESS_TOKEN = System.getProperty("access-token", "pterastatic");

  static final String CLIENT_KEY_DEFAULT = "pteracuda";

  public static final String CLIENT_KEY = System.getProperty("client-key", "unset");

  // {"cid":"P73R4CUD4"}
  static final String LOCAL_JWT =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjaWQiOiJQNzNSNENVRDQifQ"
          + ".Agj_xLqXasOzEet6Ja6hONNJ3z4D4xMvpQw0skXBaZI";

  @SneakyThrows
  public static ExpectedResponse doDelete(
      String request, String description, Integer expectedStatus) {
    Method method = Method.DELETE;
    SystemDefinitions.Service svc = systemDefinition().sandboxDataR4();
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(svc.url())
            .port(svc.port())
            .relaxedHTTPSValidation()
            .header("Content-Type", "application/json");
    log.info(
        "Expect {} DELETE '{}' is status code ({})",
        svc.urlWithApiPath() + request,
        description,
        expectedStatus);
    ExpectedResponse response =
        ExpectedResponse.of(spec.request(method, svc.urlWithApiPath() + request))
            .logAction(logAllWithTruncatedBody(2000))
            .mapper(MAPPER);
    if (expectedStatus != null) {
      response.expect(expectedStatus);
    }
    return response;
  }

  public static ExpectedResponse doGet(
      String acceptHeader, String request, Integer expectedStatus) {
    Method method = Method.GET;
    SystemDefinitions.Service svc = systemDefinition().r4();
    String clientKey = null;
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(svc.url())
            .port(svc.port())
            .relaxedHTTPSValidation()
            .header("Authorization", "Bearer " + ACCESS_TOKEN);
    if (clientKey != null) {
      spec = spec.header("client-key", clientKey);
    }
    log.info(
        "Expect {} with accept header ({}) is status code ({})",
        svc.urlWithApiPath() + request,
        acceptHeader,
        expectedStatus);
    if (acceptHeader != null) {
      spec = spec.accept(acceptHeader);
    }
    ExpectedResponse response =
        ExpectedResponse.of(spec.request(method, svc.urlWithApiPath() + request))
            .logAction(logAllWithTruncatedBody(2000))
            .mapper(MAPPER);
    if (expectedStatus != null) {
      response.expect(expectedStatus);
    }
    return response;
  }

  public static ExpectedResponse doInternalGet(
      String acceptHeader, String request, Integer expectedStatus, String clientKey) {
    Method method = Method.GET;
    SystemDefinitions.Service svc = systemDefinition().internalR4();
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(svc.url())
            .port(svc.port())
            .relaxedHTTPSValidation()
            .header("Authorization", "Bearer " + ACCESS_TOKEN);
    if (clientKey != null) {
      spec = spec.header("client-key", clientKey);
    }
    log.info(
        "Expect {} with accept header ({}) is status code ({})",
        svc.urlWithApiPath() + request,
        acceptHeader,
        expectedStatus);
    if (acceptHeader != null) {
      spec = spec.accept(acceptHeader);
    }
    ExpectedResponse response =
        ExpectedResponse.of(spec.request(method, svc.urlWithApiPath() + request))
            .logAction(logAllWithTruncatedBody(2000))
            .mapper(MAPPER);
    if (expectedStatus != null) {
      response.expect(expectedStatus);
    }
    return response;
  }

  @SneakyThrows
  public static ExpectedResponse doInternalPost(
      String request,
      Object payload,
      String description,
      Integer expectedStatus,
      String clientKey) {
    Method method = Method.POST;
    SystemDefinitions.Service svc = systemDefinition().internalR4();
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(svc.url())
            .port(svc.port())
            .relaxedHTTPSValidation()
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .header("client-key", clientKey)
            .header("Content-Type", "application/json")
            .body(MAPPER.writeValueAsString(payload));
    log.info(
        "Expect {} POST '{}' is status code ({})",
        svc.urlWithApiPath() + request,
        description,
        expectedStatus);
    ExpectedResponse response =
        ExpectedResponse.of(spec.request(method, svc.urlWithApiPath() + request))
            .logAction(logAllWithTruncatedBody(2000))
            .mapper(MAPPER);
    if (expectedStatus != null) {
      response.expect(expectedStatus);
    }
    return response;
  }

  @SneakyThrows
  public static ExpectedResponse doPost(
      String request, Object payload, String description, Integer expectedStatus) {
    Method method = Method.POST;
    SystemDefinitions.Service svc = systemDefinition().r4();
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(svc.url())
            .port(svc.port())
            .relaxedHTTPSValidation()
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .body(MAPPER.writeValueAsString(payload));
    log.info(
        "Expect {} POST '{}' is status code ({})",
        svc.urlWithApiPath() + request,
        description,
        expectedStatus);

    ExpectedResponse response =
        ExpectedResponse.of(spec.request(method, svc.urlWithApiPath() + request))
            .logAction(logAllWithTruncatedBody(2000))
            .mapper(MAPPER);
    if (expectedStatus != null) {
      response.expect(expectedStatus);
    }
    return response;
  }

  @SneakyThrows
  public static ExpectedResponse doPut(
      String request, Object payload, String description, Integer expectedStatus) {
    return doPut(request, payload, description, ACCESS_TOKEN, expectedStatus);
  }

  @SneakyThrows
  public static ExpectedResponse doPut(
      String request,
      Object payload,
      String description,
      String accessToken,
      Integer expectedStatus) {
    Method method = Method.PUT;
    SystemDefinitions.Service svc = systemDefinition().r4();
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(svc.url())
            .port(svc.port())
            .relaxedHTTPSValidation()
            .header("Authorization", "Bearer " + accessToken)
            .header("Content-Type", "application/json")
            .body(MAPPER.writeValueAsString(payload));
    log.info(
        "Expect {} PUT '{}' is status code ({})",
        svc.urlWithApiPath() + request,
        description,
        expectedStatus);
    ExpectedResponse response =
        ExpectedResponse.of(spec.request(method, svc.urlWithApiPath() + request))
            .logAction(logAllWithTruncatedBody(2000))
            .mapper(MAPPER);
    if (expectedStatus != null) {
      response.expect(expectedStatus);
    }
    return response;
  }
}
