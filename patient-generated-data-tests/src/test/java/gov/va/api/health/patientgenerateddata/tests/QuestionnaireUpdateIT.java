package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalPost;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPut;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.CLIENT_KEY_DEFAULT;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireUpdateIT {
  static Questionnaire questionnaire(String id) {
    return Questionnaire.builder().id(id).status(Questionnaire.PublicationStatus.active).build();
  }

  @BeforeAll
  static void setup() {
    // These tests alter data, but do not infinitely create more
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
    var id = systemDefinition().ids().questionnaireUpdates();
    ExpectedResponse response = doGet("application/json", "Questionnaire/" + id, null);
    if (response.response().statusCode() == 404) {
      String clientKey = System.getProperty("client-key", CLIENT_KEY_DEFAULT);
      doInternalPost("Questionnaire", questionnaire(id), "create", 201, clientKey);
    }
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
}
