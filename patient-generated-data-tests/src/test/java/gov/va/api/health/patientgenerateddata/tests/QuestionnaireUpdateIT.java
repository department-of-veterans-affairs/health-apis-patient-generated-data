package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doDelete;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalPost;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPut;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.CLIENT_KEY_DEFAULT;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.LOCAL_JWT;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import java.time.Instant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireUpdateIT {
  private static final String CLIENT_KEY = System.getProperty("client-key", CLIENT_KEY_DEFAULT);

  static Questionnaire questionnaire(String id) {
    return Questionnaire.builder().id(id).status(Questionnaire.PublicationStatus.active).build();
  }

  @BeforeAll
  static void setUp() {
    // These tests alter data, but do not infinitely create more
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
    var id = systemDefinition().ids().questionnaireUpdates();
    ExpectedResponse response = doGet("application/json", "Questionnaire/" + id, null);
    if (response.response().statusCode() == 404) {
      doInternalPost("Questionnaire", questionnaire(id), "create", 201, CLIENT_KEY);
    }
  }

  @AfterAll
  static void tearDown() {
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
    var id = systemDefinition().ids().questionnaireUpdates();
    doDelete("Questionnaire/" + id, "tear down", 200);
  }

  @Test
  void update_description() {
    Instant now = Instant.now();
    var id = systemDefinition().ids().questionnaireUpdates();
    Questionnaire questionnaire = questionnaire(id).description(now.toString());
    doPut("Questionnaire/" + id, questionnaire, "update description", 200);
    ExpectedResponse persistedResponse = doGet("application/json", "Questionnaire/" + id, 200);
    Questionnaire persisted = persistedResponse.response().as(Questionnaire.class);
    assertThat(persisted.description()).isEqualTo(now.toString());
  }

  @Test
  void update_forbidden() {
    assumeEnvironmentIn(Environment.LOCAL);
    var id = systemDefinition().ids().questionnaireUpdates();
    doPut("Questionnaire/" + id, questionnaire(id), "update description", LOCAL_JWT, 403);
  }
}
