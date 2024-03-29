package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.Requests.doInternalGet;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doInternalPost;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ManagementControllerProtectionIT {
  @BeforeAll
  static void setup() {
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
  }

  @Test
  void create_observation_invalidKey() {
    var obs = Observation.builder().id("it-bad").build();
    doInternalPost("create resource (invalid key)", "r4/Observation", obs, "NOPE", 401);
  }

  @Test
  void create_questionnaireResponse_invalidKey() {
    var qr = QuestionnaireResponse.builder().id("it-bad").build();
    doInternalPost("create resource (invalid key)", "r4/QuestionnaireResponse", qr, "NOPE", 401);
  }

  @Test
  void create_questionnaire_invalidKey() {
    var questionnaire = Questionnaire.builder().id("it-bad").build();
    doInternalPost("create resource (invalid key)", "r4/Questionnaire", questionnaire, "NOPE", 401);
  }

  @Test
  void ids_observation_invalidKey() {
    doInternalGet("r4/Observation/ids", "NOPE", 401);
  }

  @Test
  void ids_questionnaireResponse_invalidKey() {
    doInternalGet("r4/QuestionnaireResponse/ids", "NOPE", 401);
  }

  @Test
  void ids_questionnaire_invalidKey() {
    doInternalGet("r4/Questionnaire/ids", "NOPE", 401);
  }
}
