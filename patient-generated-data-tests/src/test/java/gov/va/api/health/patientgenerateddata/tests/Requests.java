package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;
import static java.util.stream.Collectors.toMap;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.Service;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.health.sentinel.ExpectedResponse;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Requests {
  static final ObjectMapper MAPPER =
      JacksonConfig.createMapper().registerModule(new Resource.ResourceModule());

  static final String ACCESS_TOKEN = System.getProperty("access-token", "pterastatic");

  static final String CLIENT_KEY = System.getProperty("client-key", "pteracuda");

  // {"cid":"P73R4CUD4"}
  static final String LOCAL_JWT =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjaWQiOiJQNzNSNENVRDQifQ"
          + ".Agj_xLqXasOzEet6Ja6hONNJ3z4D4xMvpQw0skXBaZI";

  static ExpectedResponse doDelete(String description, String request, Integer expectedStatus) {
    SystemDefinitions.Service svc = systemDefinition().sandboxDataR4();
    return doRequest(Method.DELETE, svc, description, request, null, Map.of(), expectedStatus);
  }

  static ExpectedResponse doGet(String acceptHeader, String request, Integer expectedStatus) {
    Service svc = systemDefinition().r4();
    var headers = new HashMap<String, String>();
    if (acceptHeader != null) {
      headers.put("Accept", acceptHeader);
    }
    headers.put("Authorization", "Bearer " + ACCESS_TOKEN);
    return doRequest(Method.GET, svc, null, request, null, headers, expectedStatus);
  }

  static ExpectedResponse doInternalGet(String request, String clientKey, Integer expectedStatus) {
    Service svc = systemDefinition().internalR4();
    return doRequest(
        Method.GET, svc, null, request, null, Map.of("client-key", clientKey), expectedStatus);
  }

  static ExpectedResponse doInternalPost(
      String description,
      String request,
      Resource payload,
      String clientKey,
      Integer expectedStatus) {
    Service svc = systemDefinition().internalR4();
    Map<String, String> headers =
        Map.of(
            "Authorization",
            "Bearer " + ACCESS_TOKEN,
            "client-key",
            clientKey,
            "Content-Type",
            "application/json");
    return doRequest(Method.POST, svc, description, request, payload, headers, expectedStatus);
  }

  static ExpectedResponse doPost(
      String description, String request, Resource payload, Integer expectedStatus) {
    Service svc = systemDefinition().r4();
    Map<String, String> headers =
        Map.of("Authorization", "Bearer " + ACCESS_TOKEN, "Content-Type", "application/json");
    return doRequest(Method.POST, svc, description, request, payload, headers, expectedStatus);
  }

  static ExpectedResponse doPut(
      String description,
      String request,
      Resource payload,
      String accessToken,
      Integer expectedStatus) {
    Service svc = systemDefinition().r4();
    Map<String, String> headers =
        Map.of("Authorization", "Bearer " + accessToken, "Content-Type", "application/json");
    return doRequest(Method.PUT, svc, description, request, payload, headers, expectedStatus);
  }

  @SneakyThrows
  private static ExpectedResponse doRequest(
      @NonNull Method method,
      @NonNull SystemDefinitions.Service svc,
      String description,
      @NonNull String request,
      Resource payload,
      @NonNull Map<String, String> headers,
      Integer expectedStatus) {
    Map<String, String> filteredHeaders =
        headers
            .entrySet()
            .stream()
            .filter(e -> !e.getKey().equalsIgnoreCase("Authorization"))
            .filter(e -> !e.getKey().equalsIgnoreCase("client-key"))
            .collect(toMap(e -> e.getKey(), e -> e.getValue()));
    log.info(
        "{} {}{}{}{}",
        method,
        svc.urlWithApiPath() + request,
        description == null ? "" : String.format(", '%s'", description),
        filteredHeaders.isEmpty() ? "" : ", " + filteredHeaders,
        expectedStatus == null ? "" : String.format(", expect status code '%s'", expectedStatus));
    RequestSpecification spec =
        RestAssured.given()
            .baseUri(svc.url())
            .port(svc.port())
            .relaxedHTTPSValidation()
            .headers(headers);
    if (payload != null) {
      spec = spec.body(MAPPER.writeValueAsString(payload));
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
}
