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

  private boolean bundleIncludesEntryWithId(QuestionnaireResponse.Bundle bundle, String id) {
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
    String query = String.format("?author=%s", author);
    var response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    query = String.format("?author=%s", "unknown");
    response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  void search_authored() {
    String expectedId = systemDefinition().ids().questionnaireResponse();
    String date = "2013-02-19T19:15:00Z";
    String query = String.format("?authored=%s", date);
    var response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    assertThat(bundleIncludesEntryWithId(bundle, expectedId)).isTrue();
    // offset timezone
    query = "?authored=2013-02-19T14:15:00-05:00";
    response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    assertThat(bundleIncludesEntryWithId(bundle, expectedId)).isTrue();
    // less than
    query = "?authored=lt2014-01-01T00:00:00Z";
    response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    assertThat(bundleIncludesEntryWithId(bundle, expectedId)).isTrue();
    // in between
    query =
        String.format(
            "?authored=gt%s&authored=lt%s", "2013-02-19T00:00:00Z", "2013-02-19T23:59:00Z");
    response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    assertThat(bundleIncludesEntryWithId(bundle, expectedId)).isTrue();
    // in between, nothing found
    query =
        String.format(
            "?authored=gt%s&authored=lt%s", "1998-01-01T00:00:00Z", "1998-01-01T01:00:00Z");
    response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(0);
  }

  @Test
  void search_id() {
    var id = systemDefinition().ids().questionnaireResponse();
    var response = doGet("application/json", "QuestionnaireResponse?_id=" + id, 200);
    response.expectValid(QuestionnaireResponse.Bundle.class);
  }

  @Test
  void search_questionnaire() {
    String questionnaire = systemDefinition().ids().questionnaire();
    String query = String.format("?questionnaire=%s", questionnaire);
    var response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_questionnaireUnknown() {
    String query = String.format("?questionnaire=%s", "unknown");
    var response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  void search_questionnaire_csv() {
    String questionnaireList = systemDefinition().ids().questionnaireList();
    String query = String.format("?questionnaire=%s", questionnaireList);
    var response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(2);
  }

  @Test
  void search_subject() {
    String subject = systemDefinition().ids().questionnaireResponseSubject();
    String query = String.format("?subject=%s", subject);
    var response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    query = String.format("?subject=%s", "unknown");
    response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  void search_tag_codeWithAnySystem() {
    String tagCode = systemDefinition().ids().questionnaireResponseMetaTag().code();
    String query = String.format("?_tag=%s", tagCode);
    var response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_tag_codeWithNoSystem() {
    String tagCode = systemDefinition().ids().questionnaireResponseMetaTag().code();
    String query = String.format("?_tag=%s", "|" + tagCode);
    var response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  void search_tag_systemWithAnyCode() {
    String tagSystem = systemDefinition().ids().questionnaireResponseMetaTag().system();
    String query = String.format("?_tag=%s", tagSystem + "|");
    var response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }

  @Test
  void search_tag_systemWithCode() {
    String tagCode = systemDefinition().ids().questionnaireResponseMetaTag().code();
    String tagSystem = systemDefinition().ids().questionnaireResponseMetaTag().system();
    String query = String.format("?_tag=%s", tagSystem + "|" + tagCode);
    var response = doGet("application/json", "QuestionnaireResponse" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
  }
}
