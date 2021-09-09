package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalGet;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.CLIENT_KEY_DEFAULT;

import org.junit.jupiter.api.Test;

public class ManagementControllerGetIdsIT {
  private static final String CLIENT_KEY = System.getProperty("client-key", CLIENT_KEY_DEFAULT);

  @Test
  void getObservationIds() {
    doInternalGet("application/json", "Observation/ids", CLIENT_KEY,200 );
  }

  @Test
  void getQuestionnaireIds() {
    doInternalGet("application/json", "Questionnaire/ids",CLIENT_KEY, 200 );
  }

  @Test
  void getQuestionnaireResponseIds() {
    doInternalGet("application/json", "QuestionnaireResponse/ids", CLIENT_KEY,200 );
  }
}
