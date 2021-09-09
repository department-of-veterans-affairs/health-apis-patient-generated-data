package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.CLIENT_KEY_DEFAULT;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalGet;

import org.junit.jupiter.api.Test;

public class ManagementControllerGetIdsIT {
  private static final String CLIENT_KEY = System.getProperty("client-key", CLIENT_KEY_DEFAULT);

  @Test
  void getObservationIds() {
    doInternalGet("Observation/ids", CLIENT_KEY, 200);
  }

  @Test
  void getQuestionnaireIds() {
    doInternalGet("Questionnaire/ids", CLIENT_KEY, 200);
  }

  @Test
  void getQuestionnaireResponseIds() {
    doInternalGet("QuestionnaireResponse/ids", CLIENT_KEY, 200);
  }
}
