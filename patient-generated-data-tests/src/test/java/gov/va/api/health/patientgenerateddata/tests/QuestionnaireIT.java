package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(
        Environment.LOCAL,
        Environment.QA,
        Environment.STAGING,
        Environment.STAGING_LAB,
        Environment.LAB);
  }

  @Test
  void read() {
    String id = systemDefinition().ids().questionnaire();
    doGet(null, "Questionnaire/" + id, 200);
    doGet("application/json", "Questionnaire/" + id, 200);
    doGet("application/fhir+json", "Questionnaire/" + id, 200);
  }

  @Test
  void read_notFound() {
    doGet("application/json", "Questionnaire/5555555", 404);
  }

  @Test
  void search_contextTypeValue() {
    assumeEnvironmentIn(Environment.LOCAL);
    // Environment.QA, Environment.STAGING, Environment.STAGING_LAB Environment.LAB
    String arg = "venue$https://staff.apps.va.gov/VistaEmrService/clinics|534/12975";
    String query = String.format("?context-type-value=%s", arg);
    var response = doGet("application/json", "Questionnaire" + query, 200);
    Questionnaire.Bundle bundle = response.expectValid(Questionnaire.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_id() {
    assumeEnvironmentIn(
        Environment.LOCAL,
        Environment.QA,
        Environment.STAGING,
        Environment.STAGING_LAB,
        Environment.LAB);
    var id = systemDefinition().ids().questionnaire();
    var response = doGet("application/json", "Questionnaire?_id=" + id, 200);
    response.expectValid(Questionnaire.Bundle.class);
  }
}
