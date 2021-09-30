package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.Requests.CLIENT_KEY;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doInternalGet;

import org.junit.jupiter.api.Test;

public class ManagementControllerGetIdsIT {
  @Test
  void observationIds() {
    doInternalGet("r4/Observation/ids", CLIENT_KEY, 200);
  }

  @Test
  void questionnaireIds() {
    doInternalGet("r4/Questionnaire/ids", CLIENT_KEY, 200);
  }

  @Test
  void questionnaireResponseIds() {
    doInternalGet("r4/QuestionnaireResponse/ids", CLIENT_KEY, 200);
  }
}
