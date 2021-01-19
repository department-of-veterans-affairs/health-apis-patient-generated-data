package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalPost;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.Questionnaire.PublicationStatus;
import gov.va.api.health.sentinel.Environment;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

public class ManagementControllerIT {

  private static Questionnaire _questionnaire(String id) {
    return Questionnaire.builder().id(id).title("x").status(PublicationStatus.active).build();
  }

  @BeforeAll
  static void setup() {
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
  }

  @Test
  public void create_questionnaire_invalid_key() {
    var questionnaire = _questionnaire("it-bad" + System.currentTimeMillis());
    doInternalPost("Questionnaire", questionnaire, "create resource (invalid key)", 401, "NOPE");
  }
}
