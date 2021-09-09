package gov.va.api.health.patientgenerateddata.tests;

import static com.google.common.base.Preconditions.checkArgument;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.ExpectedResponse.logAllWithTruncatedBody;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
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
public class RequestUtils {
  static final ObjectMapper MAPPER =
      JacksonConfig.createMapper().registerModule(new Resource.ResourceModule());

  static final String ACCESS_TOKEN = System.getProperty("access-token", "pterastatic");

  static final String CLIENT_KEY = System.getProperty("client-key", "pteracuda");

  // {"cid":"P73R4CUD4"}
  static final String LOCAL_JWT =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjaWQiOiJQNzNSNENVRDQifQ"
          + ".Agj_xLqXasOzEet6Ja6hONNJ3z4D4xMvpQw0skXBaZI";

  @SneakyThrows
  public static ExpectedResponse doDelete(
      String description, String request, Integer expectedStatus) {
    return doRequest(
        description,
        systemDefinition().sandboxDataR4(),
        Method.DELETE,
        request,
        null,
        Map.of("Authorization", "Bearer " + ACCESS_TOKEN),
        expectedStatus);
  }

  public static ExpectedResponse doGet(
      String acceptHeader, String request, Integer expectedStatus) {
    return doRequest(
        null,
        systemDefinition().r4(),
        Method.GET,
        request,
        null,
        acceptHeader == null
            ? Map.of("Authorization", "Bearer " + ACCESS_TOKEN)
            : Map.of("Accept", acceptHeader, "Authorization", "Bearer " + ACCESS_TOKEN),
        expectedStatus);
  }

  public static ExpectedResponse doInternalGet(
      String request, String clientKey, Integer expectedStatus) {
    return doRequest(
        null,
        systemDefinition().internalR4(),
        Method.GET,
        request,
        null,
        Map.of("client-key", clientKey),
        expectedStatus);
  }

  @SneakyThrows
  public static ExpectedResponse doInternalPost(
      String description,
      String request,
      Resource payload,
      String clientKey,
      Integer expectedStatus) {
    return doRequest(
        description,
        systemDefinition().internalR4(),
        Method.POST,
        request,
        payload,
        Map.of(
            "Authorization",
            "Bearer " + ACCESS_TOKEN,
            "client-key",
            clientKey,
            "Content-Type",
            "application/json"),
        expectedStatus);
  }

  public static ExpectedResponse doPost(
      String description, String request, Resource payload, Integer expectedStatus) {
    return doRequest(
        description,
        systemDefinition().r4(),
        Method.POST,
        request,
        payload,
        Map.of("Authorization", "Bearer " + ACCESS_TOKEN, "Content-Type", "application/json"),
        expectedStatus);
  }

  public static ExpectedResponse doPut(
      String description,
      String request,
      Resource payload,
      String accessToken,
      Integer expectedStatus) {
    return doRequest(
        description,
        systemDefinition().r4(),
        Method.PUT,
        request,
        payload,
        Map.of("Authorization", "Bearer " + accessToken, "Content-Type", "application/json"),
        expectedStatus);
  }

  @SneakyThrows
  private static ExpectedResponse doRequest(
      String description,
      @NonNull SystemDefinitions.Service svc,
      @NonNull Method method,
      @NonNull String request,
      Resource payload,
      @NonNull Map<String, String> headers,
      Integer expectedStatus) {
    checkArgument(!headers.isEmpty());
    Map<String, String> filteredHeaders =
        headers
            .entrySet()
            .stream()
            .filter(e -> !e.getKey().equals("Authorization"))
            .filter(e -> !e.getKey().equals("client-key"))
            .collect(toMap(e -> e.getKey(), e -> e.getValue()));
    log.info(
        "{} {}{}{}{}",
        method,
        svc.urlWithApiPath() + request,
        description == null ? "" : String.format(", '%s'", description),
        !filteredHeaders.isEmpty() ? "" : ", " + filteredHeaders,
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
