package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doGet;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireResponseIT {
  @BeforeAll
  static void assumeEnvironment() {
    assumeEnvironmentIn(
        Environment.LOCAL,
        Environment.QA,
        Environment.STAGING,
        Environment.STAGING_LAB,
        Environment.LAB);
  }

  private static boolean bundleIncludesEntryWithId(QuestionnaireResponse.Bundle bundle, String id) {
    var count = bundle.entry().stream().filter((e) -> e.resource().id().equals(id)).count();
    return count > 0;
  }

  @Test
  void read() {
    var id = systemDefinition().ids().questionnaireResponse();
    doGet(null, "QuestionnaireResponse/" + id, 200);
    doGet("application/json", "QuestionnaireResponse/" + id, 200);
    doGet("application/fhir+json", "QuestionnaireResponse/" + id, 200);
  }

  @Test
  void read_notFound() {
    doGet("application/json", "QuestionnaireResponse/55555555", 404);
  }

  @Test
  void search_author() {
    String author = systemDefinition().ids().questionnaireResponseAuthor();
    String query = String.format("QuestionnaireResponse?author=%s", author);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_author_unknown() {
    String query = String.format("QuestionnaireResponse?author=%s", "unknown");
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  void search_authored_exact() {
    String id = systemDefinition().ids().questionnaireResponse();
    String query = String.format("QuestionnaireResponse?authored=%s", "2013-02-19T19:15:00Z");
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    assertThat(bundleIncludesEntryWithId(bundle, id)).isTrue();
  }

  @Test
  void search_authored_inBetween() {
    String id = systemDefinition().ids().questionnaireResponse();
    String query =
        String.format(
            "QuestionnaireResponse?authored=gt%s&authored=lt%s",
            "2013-02-19T00:00:00Z", "2013-02-19T23:59:00Z");
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    assertThat(bundleIncludesEntryWithId(bundle, id)).isTrue();
  }

  @Test
  void search_authored_inBetweenEmpty() {
    String query =
        String.format(
            "QuestionnaireResponse?authored=gt%s&authored=lt%s",
            "1998-01-01T00:00:00Z", "1998-01-01T01:00:00Z");
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(0);
  }

  @Test
  void search_authored_lessThan() {
    String id = systemDefinition().ids().questionnaireResponse();
    String query = String.format("QuestionnaireResponse?authored=%s", "lt2014-01-01T00:00:00Z");
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    assertThat(bundleIncludesEntryWithId(bundle, id)).isTrue();
  }

  @Test
  void search_authored_offsetTimezone() {
    String id = systemDefinition().ids().questionnaireResponse();
    String query = String.format("QuestionnaireResponse?authored=%s", "2013-02-19T14:15:00-05:00");
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    assertThat(bundleIncludesEntryWithId(bundle, id)).isTrue();
  }

  @Test
  void search_id() {
    var id = systemDefinition().ids().questionnaireResponse();
    var response = doGet("application/json", "QuestionnaireResponse?_id=" + id, 200);
    response.expectValid(QuestionnaireResponse.Bundle.class);
  }

  @Test
  void search_id_csv() {
    var ids = String.join(",", systemDefinition().ids().questionnaireResponseList());
    var query = String.format("QuestionnaireResponse?_id=%s", ids);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(2);
  }

  @Test
  void search_questionnaire() {
    String questionnaire = systemDefinition().ids().questionnaire();
    String query = String.format("QuestionnaireResponse?questionnaire=%s", questionnaire);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_questionnaireUnknown() {
    String query = String.format("QuestionnaireResponse?questionnaire=%s", "unknown");
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  void search_questionnaire_csv() {
    var questionnaireList = String.join(",", systemDefinition().ids().questionnaireList());
    String query = String.format("QuestionnaireResponse?questionnaire=%s", questionnaireList);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(2);
  }

  @Test
  void search_source() {
    assumeEnvironmentIn(
        Environment.LOCAL,
        Environment.QA,
        Environment.STAGING,
        Environment.STAGING_LAB,
        Environment.LAB);
    String source = systemDefinition().ids().questionnaireResponseSource();
    String query = String.format("QuestionnaireResponse?source=%s", source);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_subject() {
    String subject = systemDefinition().ids().questionnaireResponseSubject();
    String query = String.format("QuestionnaireResponse?subject=%s", subject);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_subject_unknown() {
    String query = String.format("QuestionnaireResponse?subject=%s", "unknown");
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  void search_tag_codeWithAnySystem() {
    String code = systemDefinition().ids().questionnaireResponseMetas().commonTag().code();
    String query = String.format("QuestionnaireResponse?_tag=%s", code);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_tag_codeWithNoSystem() {
    String code = systemDefinition().ids().questionnaireResponseMetas().applicationTag().code();
    String query = String.format("QuestionnaireResponse?_tag=|%s", code);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  void search_tag_csv() {
    String system = systemDefinition().ids().questionnaireResponseMetas().applicationTag().system();
    String code = systemDefinition().ids().questionnaireResponseMetas().applicationTag().code();
    String system2 = systemDefinition().ids().questionnaireResponseMetas().commonTag().system();
    String code2 = systemDefinition().ids().questionnaireResponseMetas().commonTag().code();
    String query =
        String.format("QuestionnaireResponse?_tag=%s|%s,%s|%s", system, code, system2, code2);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(1);
  }

  @Test
  void search_tag_systemWithAnyCode() {
    String system = systemDefinition().ids().questionnaireResponseMetas().applicationTag().system();
    String query = String.format("QuestionnaireResponse?_tag=%s|", system);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_tag_systemWithCode() {
    String system = systemDefinition().ids().questionnaireResponseMetas().applicationTag().system();
    String code = systemDefinition().ids().questionnaireResponseMetas().applicationTag().code();
    String query = String.format("QuestionnaireResponse?_tag=%s|%s", system, code);
    var response = doGet("application/json", query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }
}
