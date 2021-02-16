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
  void search_contextTypeValue_codeWithAnySystem() {
    String query = systemDefinition().ids().questionnaireContextTypeValue().codeWithAnySystem();
    var response =
        doGet("application/json", String.format("Questionnaire?context-type-value=%s", query), 200);
    Questionnaire.Bundle bundle = response.expectValid(Questionnaire.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_contextTypeValue_codeWithNoSystem() {
    String query = systemDefinition().ids().questionnaireContextTypeValue().codeWithNoSystem();
    var response =
        doGet("application/json", String.format("Questionnaire?context-type-value=%s", query), 200);
    Questionnaire.Bundle bundle = response.expectValid(Questionnaire.Bundle.class);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  void search_contextTypeValue_systemAndCode() {
    String query = systemDefinition().ids().questionnaireContextTypeValue().systemAndCode();
    var response =
        doGet("application/json", String.format("Questionnaire?context-type-value=%s", query), 200);
    Questionnaire.Bundle bundle = response.expectValid(Questionnaire.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_contextTypeValue_systemWithAnyCode() {
    String query = systemDefinition().ids().questionnaireContextTypeValue().systemWithAnyCode();
    var response =
        doGet("application/json", String.format("Questionnaire?context-type-value=%s", query), 200);
    Questionnaire.Bundle bundle = response.expectValid(Questionnaire.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_id() {
    var id = systemDefinition().ids().questionnaire();
    var response = doGet("application/json", "Questionnaire?_id=" + id, 200);
    response.expectValid(Questionnaire.Bundle.class);
  }

  @Test
  void search_id_csv() {
    var ids = systemDefinition().ids().questionnaireList();
    var response = doGet("application/json", "Questionnaire?_id=" + ids, 200);
    Questionnaire.Bundle bundle = response.expectValid(Questionnaire.Bundle.class);
    assertThat(bundle.entry()).hasSize(2);
  }
}
