package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makePutRequest;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.makeRequest;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.serializePayload;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
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
    makeRequest(null, "QuestionnaireResponse/3141", 200);
    makeRequest("application/json", "QuestionnaireResponse/3141", 200);
    makeRequest("application/fhir+json", "QuestionnaireResponse/3141", 200);
  }

  @Test
  void read_notFound() {
    makeRequest("application/json", "QuestionnaireResponse/55555555", 404);
  }

  @Test
  void update_authored() {
    ExpectedResponse response = makeRequest("application/json", "QuestionnaireResponse/3141", 200);
    QuestionnaireResponse orig = response.response().as(QuestionnaireResponse.class);
    QuestionnaireResponse updated =
        response.response().as(QuestionnaireResponse.class).authored("2013-03-04T00:00:00Z");
    makePutRequest(
        "QuestionnaireResponse/3141", 200, serializePayload(updated), "update authored date");
    ExpectedResponse persistedResponse =
        makeRequest("application/json", "QuestionnaireResponse/3141", 200);
    QuestionnaireResponse persisted = persistedResponse.response().as(QuestionnaireResponse.class);
    assertThat(persisted.authored()).isEqualTo("2013-03-04T00:00:00Z");
    // Restore original
    makePutRequest(
        "QuestionnaireResponse/3141", 200, serializePayload(orig), "cleanup, restore original");
  }
}
