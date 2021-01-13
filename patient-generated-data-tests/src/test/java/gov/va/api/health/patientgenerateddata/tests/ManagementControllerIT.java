package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalPost;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.CLIENT_KEY_DEFAULT;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.Questionnaire.PublicationStatus;
import gov.va.api.health.sentinel.Environment;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

public class ManagementControllerIT {

  @BeforeAll
  static void assumeEnvironment() {
    // These tests invent data that will not be cleaned up
    // To avoid polluting the database, they should only run locally
    assumeEnvironmentIn(Environment.LOCAL);
  }

  @Test
  public void create_questionnaire() {
    var questionnaire = questionnaire("0000");
    doInternalPost(
        "Questionnaire",
        questionnaire,
        "create resource",
        System.getProperty("client-key", CLIENT_KEY_DEFAULT),
        201);
  }

  @Test
  public void create_questionnaire_invalid_key() {
    var questionnaire = questionnaire("0001");
    doInternalPost("Questionnaire", questionnaire, "create resource (invalid key)", "NOPE", 401);
  }

  private Questionnaire questionnaire(String id) {
    return Questionnaire.builder().id(id).title("x").status(PublicationStatus.active).build();
  }
}
