package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPut;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.serializePayload;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse.Status;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import java.time.Instant;
import java.time.temporal.ChronoField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireResponseIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(
        Environment.LOCAL,
        Environment.QA,
        Environment.STAGING,
        Environment.STAGING_LAB,
        Environment.LAB);
    loadInitialResource();
  }

  private static void loadInitialResource() {
    var id = systemDefinition().ids().questionnaireResponse();
    QuestionnaireResponse qr = questionnaireResponse(id);
    RequestUtils.doPut(
        "QuestionnaireResponse/" + id, serializePayload(qr), "load initial resource", true);
  }

  static QuestionnaireResponse questionnaireResponse(String id) {
    return QuestionnaireResponse.builder().id(id).status(Status.completed).build();
  }

  @Test
  void read() {
    var id = systemDefinition().ids().questionnaireResponse();
    doGet(null, "QuestionnaireResponse/" + id, 200);
    doGet("application/json", "QuestionnaireResponse/" + id, 200);
    doGet("application/fhir+json", "QuestionnaireResponse/" + id, 200);
  }

  @Test
  void read_notFound() {
    doGet("application/json", "QuestionnaireResponse/55555555", 404);
  }

  @Test
  void search_authored() {
    var id = systemDefinition().ids().questionnaireResponse();
    String date = "1999-02-02T12:00:00Z";
    QuestionnaireResponse qr = questionnaireResponse(id).authored(date);
    RequestUtils.doPut(
        "QuestionnaireResponse/" + id, serializePayload(qr), "set authored to 1999", true);
    String query = String.format("?authored=%s", date);
    var response = doGet("application/json", "QuestionnaireResponse/" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(1);

    // less than
    query = "?authored=lt2000-01-01T00:00:00Z";
    response = doGet("application/json", "QuestionnaireResponse/" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    // in between
    query =
        String.format(
            "?authored=gt%s&authored=lt%s", "1999-01-01T00:00:00Z", "1999-03-01T00:00:00Z");
    response = doGet("application/json", "QuestionnaireResponse/" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    // in between, nothing found
    query =
        String.format(
            "?authored=gt%s&authored=lt%s", "1998-01-01T00:00:00Z", "1998-01-01T01:00:00Z");
    response = doGet("application/json", "QuestionnaireResponse/" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(0);
  }

  @Test
  void search_id() {
    var id = systemDefinition().ids().questionnaireResponse();
    var response = doGet("application/json", "QuestionnaireResponse/?_id=" + id, 200);
    response.expectValid(QuestionnaireResponse.Bundle.class);
  }

  @Test
  void update_authored() {
    var id = systemDefinition().ids().questionnaireResponse();
    Instant now = Instant.now().with(ChronoField.NANO_OF_SECOND, 0);
    QuestionnaireResponse qr = questionnaireResponse(id).authored(now.toString());
    doPut("QuestionnaireResponse/" + id, serializePayload(qr), "update authored date", 200);
    ExpectedResponse persistedResponse =
        doGet("application/json", "QuestionnaireResponse/" + id, 200);
    QuestionnaireResponse persisted = persistedResponse.response().as(QuestionnaireResponse.class);
    assertThat(persisted.authored()).isEqualTo(now.toString());
  }
}
