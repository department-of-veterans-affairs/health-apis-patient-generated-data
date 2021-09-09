package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.Requests.CLIENT_KEY;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doInternalGet;

import org.junit.jupiter.api.Test;

public class ManagementControllerGetIdsIT {
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
