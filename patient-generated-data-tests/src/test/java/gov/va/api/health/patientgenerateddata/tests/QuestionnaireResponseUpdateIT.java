package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.CLIENT_KEY_DEFAULT;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.LOCAL_JWT;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doDelete;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalPost;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPut;
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
      doInternalPost("create", "QuestionnaireResponse", questionnaireResponse(id), CLIENT_KEY, 201);
    }
  }

  @AfterAll
  static void tearDown() {
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
    var id = systemDefinition().ids().questionnaireResponseUpdates();
    doDelete("tear down", "QuestionnaireResponse/" + id, 200);
  }

  @Test
  void update_author() {
    Instant now = Instant.now();
    Reference ref = Reference.builder().reference("Resource/" + now.toString()).build();
    var id = systemDefinition().ids().questionnaireResponseUpdates();
    QuestionnaireResponse qr = questionnaireResponse(id).author(ref);
    doPut("update author", "QuestionnaireResponse/" + id, qr, 200);
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
    doPut("update authored date", "QuestionnaireResponse/" + id, qr, 200);
    ExpectedResponse persistedResponse =
        doGet("application/json", "QuestionnaireResponse/" + id, 200);
    QuestionnaireResponse persisted = persistedResponse.response().as(QuestionnaireResponse.class);
    assertThat(persisted.authored()).isEqualTo(now.toString());
  }

  @Test
  void update_forbidden() {
    assumeEnvironmentIn(Environment.LOCAL);
    var id = systemDefinition().ids().questionnaireResponseUpdates();
    doPut(
        "update subject", "QuestionnaireResponse/" + id, questionnaireResponse(id), LOCAL_JWT, 403);
  }

  @Test
  void update_subject() {
    Instant now = Instant.now();
    Reference ref = Reference.builder().reference("Resource/" + now.toString()).build();
    var id = systemDefinition().ids().questionnaireResponseUpdates();
    QuestionnaireResponse qr = questionnaireResponse(id).subject(ref);
    doPut("update subject", "QuestionnaireResponse/" + id, qr, 200);
    ExpectedResponse persistedResponse =
        doGet("application/json", "QuestionnaireResponse/" + id, 200);
    QuestionnaireResponse persisted = persistedResponse.response().as(QuestionnaireResponse.class);
    assertThat(persisted.subject().reference()).endsWith(now.toString());
  }
}
