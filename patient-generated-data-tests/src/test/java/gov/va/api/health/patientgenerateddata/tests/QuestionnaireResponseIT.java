package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makePutRequest;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
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
    makePutRequest(
        "QuestionnaireResponse/" + id, serializePayload(qr), "load initial resource", true);
  }

  static QuestionnaireResponse questionnaireResponse(String id) {
    return QuestionnaireResponse.builder().id(id).status(Status.completed).build();
  }

  @Test
  void read() {
    var id = systemDefinition().ids().questionnaireResponse();
    makeRequest(null, "QuestionnaireResponse/" + id, 200);
    makeRequest("application/json", "QuestionnaireResponse/" + id, 200);
    makeRequest("application/fhir+json", "QuestionnaireResponse/" + id, 200);
  }

  @Test
  void read_notFound() {
    makeRequest("application/json", "QuestionnaireResponse/55555555", 404);
  }

  @Test
  void update_authored() {
    var id = systemDefinition().ids().questionnaireResponse();
    Instant now = Instant.now().with(ChronoField.NANO_OF_SECOND, 0);
    QuestionnaireResponse qr = questionnaireResponse(id).authored(now.toString());
    makePutRequest(
        "QuestionnaireResponse/" + id, serializePayload(qr), "update authored date", 200);
    ExpectedResponse persistedResponse =
        makeRequest("application/json", "QuestionnaireResponse/" + id, 200);
    QuestionnaireResponse persisted = persistedResponse.response().as(QuestionnaireResponse.class);
    assertThat(persisted.authored()).isEqualTo(now.toString());
  }
}
