package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.Requests.CLIENT_KEY;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doInternalGet;

import org.junit.jupiter.api.Test;

public class ManagementControllerGetIdsIT {
  @Test
  void observationIds() {
    doInternalGet("Observation/ids", CLIENT_KEY, 200);
  }

  @Test
  void questionnaireIds() {
    doInternalGet("Questionnaire/ids", CLIENT_KEY, 200);
  }

  @Test
  void questionnaireResponseIds() {
    doInternalGet("QuestionnaireResponse/ids", CLIENT_KEY, 200);
  }
}
