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

  @Test
  void read() {
    var id = systemDefinition().ids().questionnaireResponseSynthetic();
    doGet(null, "QuestionnaireResponse/" + id, 200);
    doGet("application/json", "QuestionnaireResponse/" + id, 200);
    doGet("application/fhir+json", "QuestionnaireResponse/" + id, 200);
  }

  @Test
  void read_notFound() {
    doGet("application/json", "QuestionnaireResponse/55555555", 404);
  }

  @Test
  void search_authored() {
    var id = systemDefinition().ids().questionnaireResponseSynthetic();
    String date = "2013-02-19T19:15:00Z";
    String query = String.format("?authored=%s", date);
    var response = doGet("application/json", "QuestionnaireResponse/" + query, 200);
    QuestionnaireResponse.Bundle bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(1);

    // offset timezone
    query = "?authored=2013-02-19T14:15:00-05:00";
    response = doGet("application/json", "QuestionnaireResponse/" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(1);

    // less than
    query = "?authored=lt2014-01-01T00:00:00Z";
    response = doGet("application/json", "QuestionnaireResponse/" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    // in between
    query =
        String.format(
            "?authored=gt%s&authored=lt%s", "2013-02-19T00:00:00Z", "2013-02-19T23:59:00Z");
    response = doGet("application/json", "QuestionnaireResponse/" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSizeGreaterThan(0);
    // in between, nothing found
    query =
        String.format(
            "?authored=gt%s&authored=lt%s", "1998-01-01T00:00:00Z", "1998-01-01T01:00:00Z");
    response = doGet("application/json", "QuestionnaireResponse/" + query, 200);
    bundle = response.expectValid(QuestionnaireResponse.Bundle.class);
    assertThat(bundle.entry()).hasSize(0);
  }

  @Test
  void search_id() {
    var id = systemDefinition().ids().questionnaireResponse();
    var response = doGet("application/json", "QuestionnaireResponse/?_id=" + id, 200);
    response.expectValid(QuestionnaireResponse.Bundle.class);
  }
}
