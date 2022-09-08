package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.Requests.CLIENT_KEY;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doInternalDelete;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ManagementControllerPurgeIT {
  @BeforeAll
  static void setUp() {
    assumeEnvironmentNotIn(Environment.PROD);
  }

  @Test
  void purge() {
    doInternalDelete("archive/r4/purge", CLIENT_KEY, 200).expectListOf(String.class);
  }
}
