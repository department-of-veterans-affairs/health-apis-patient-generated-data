package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPut;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.serializePayload;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse.Status;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import java.time.Instant;
import java.time.temporal.ChronoField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireResponseWritesIT {
  static QuestionnaireResponse questionnaireResponse(String id) {
    return QuestionnaireResponse.builder().id(id).status(Status.completed).build();
  }

  @BeforeAll
  static void setup() {
    // These tests alter data, but do not infinitely create more
    // Do not run in SLA'd environments
    assumeEnvironmentIn(Environment.LOCAL);
    // Environment.QA, Environment.STAGING, Environment.STAGING_LAB

    var id = systemDefinition().idsGenerated().questionnaireResponse();
    ExpectedResponse existing = doGet("application/json", "QuestionnaireResponse/" + id, null);
    if (existing.response().statusCode() == 404) {
      QuestionnaireResponse qr = questionnaireResponse(id);
      doPut("QuestionnaireResponse/" + id, serializePayload(qr), "load initial resource", 201);
    }
  }

  @Test
  void update_author() {
    var id = systemDefinition().idsGenerated().questionnaireResponse();
    Reference ref =
        Reference.builder().id(systemDefinition().ids().questionnaireResponseAuthor()).build();
    QuestionnaireResponse qr = questionnaireResponse(id).author(ref);
    doPut("QuestionnaireResponse/" + id, serializePayload(qr), "update author", 200);
    ExpectedResponse persistedResponse =
        doGet("application/json", "QuestionnaireResponse/" + id, 200);
    QuestionnaireResponse persisted = persistedResponse.response().as(QuestionnaireResponse.class);
    assertThat(persisted.author()).isEqualTo(ref);
  }

  @Test
  void update_authored() {
    var id = systemDefinition().idsGenerated().questionnaireResponse();
    Instant now = Instant.now().with(ChronoField.NANO_OF_SECOND, 0);
    QuestionnaireResponse qr = questionnaireResponse(id).authored(now.toString());
    doPut("QuestionnaireResponse/" + id, serializePayload(qr), "update authored date", 200);
    ExpectedResponse persistedResponse =
        doGet("application/json", "QuestionnaireResponse/" + id, 200);
    QuestionnaireResponse persisted = persistedResponse.response().as(QuestionnaireResponse.class);
    assertThat(persisted.authored()).isEqualTo(now.toString());
  }
}
