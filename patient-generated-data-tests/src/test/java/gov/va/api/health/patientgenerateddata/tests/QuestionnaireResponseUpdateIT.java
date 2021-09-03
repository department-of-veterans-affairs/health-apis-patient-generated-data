package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doDelete;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalPost;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPut;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.CLIENT_KEY_DEFAULT;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import java.time.Instant;
import java.time.temporal.ChronoField;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireResponseUpdateIT {
  private static final String CLIENT_KEY = System.getProperty("client-key", CLIENT_KEY_DEFAULT);

  static QuestionnaireResponse questionnaireResponse(String id) {
    return QuestionnaireResponse.builder()
        .id(id)
        .status(QuestionnaireResponse.Status.completed)
        .build();
  }

  @BeforeAll
  static void setUp() {
    // These tests alter data, but do not infinitely create more
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
    var id = systemDefinition().ids().questionnaireResponseUpdates();
    ExpectedResponse response = doGet("application/json", "QuestionnaireResponse/" + id, null);
    if (response.response().statusCode() == 404) {
      doInternalPost("QuestionnaireResponse", questionnaireResponse(id), "create", 201, CLIENT_KEY);
    }
  }

  @AfterAll
  static void tearDown() {
    var id = systemDefinition().ids().questionnaireResponseUpdates();
    doDelete("QuestionnaireResponse/" + id, "tear down", 200);
  }

  @Test
  void update_author() {
    Instant now = Instant.now();
    Reference ref = Reference.builder().reference("Resource/" + now.toString()).build();
    var id = systemDefinition().ids().questionnaireResponseUpdates();
    QuestionnaireResponse qr = questionnaireResponse(id).author(ref);
    doPut("QuestionnaireResponse/" + id, qr, "update author", 200);
    ExpectedResponse persistedResponse =
        doGet("application/json", "QuestionnaireResponse/" + id, 200);
    QuestionnaireResponse persisted = persistedResponse.response().as(QuestionnaireResponse.class);
    assertThat(persisted.author().reference()).endsWith(now.toString());
  }

  @Test
  void update_authored() {
    var id = systemDefinition().ids().questionnaireResponseUpdates();
    Instant now = Instant.now().with(ChronoField.NANO_OF_SECOND, 0);
    QuestionnaireResponse qr = questionnaireResponse(id).authored(now.toString());
    doPut("QuestionnaireResponse/" + id, qr, "update authored date", 200);
    ExpectedResponse persistedResponse =
        doGet("application/json", "QuestionnaireResponse/" + id, 200);
    QuestionnaireResponse persisted = persistedResponse.response().as(QuestionnaireResponse.class);
    assertThat(persisted.authored()).isEqualTo(now.toString());
  }

  @Test
  void update_subject() {
    Instant now = Instant.now();
    Reference ref = Reference.builder().reference("Resource/" + now.toString()).build();
    var id = systemDefinition().ids().questionnaireResponseUpdates();
    QuestionnaireResponse qr = questionnaireResponse(id).subject(ref);
    doPut("QuestionnaireResponse/" + id, qr, "update subject", 200);
    ExpectedResponse persistedResponse =
        doGet("application/json", "QuestionnaireResponse/" + id, 200);
    QuestionnaireResponse persisted = persistedResponse.response().as(QuestionnaireResponse.class);
    assertThat(persisted.subject().reference()).endsWith(now.toString());
  }
}
