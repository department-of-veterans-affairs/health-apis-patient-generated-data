package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.Requests.CLIENT_KEY;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doDelete;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doGet;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doInternalGet;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doInternalPost;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doPost;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.sentinel.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QuestionnaireResponseDeleteIT {
  static final String TEST_ID = "it" + System.currentTimeMillis();

  @BeforeAll
  static void assumeEnvironment() {
    // These tests invent new data and then archive it
    // Do not run in SLA'd environments
    assumeEnvironmentIn(
        Environment.LOCAL, Environment.QA, Environment.STAGING, Environment.STAGING_LAB);
  }

  static QuestionnaireResponse questionnaireResponse(String id) {
    return QuestionnaireResponse.builder()
        .id(id)
        .status(QuestionnaireResponse.Status.completed)
        .build();
  }

  @Test
  void archivedDelete() {
    var response =
        doPost("create resource", "QuestionnaireResponse", questionnaireResponse(null), 201);
    String id = response.expectValid(QuestionnaireResponse.class).id();

    doDelete("archive the resource", "QuestionnaireResponse/" + id, 204);
    doInternalGet("archive/r4/QuestionnaireResponse/" + id, CLIENT_KEY, 200);
    doGet("application/json", "QuestionnaireResponse/" + id, 404);
  }

  @Test
  void archivedOverwrite() {
    QuestionnaireResponse questionnaireResponse = questionnaireResponse(TEST_ID);

    doInternalPost(
        "create resource with known ID",
        "r4/QuestionnaireResponse",
        questionnaireResponse,
        CLIENT_KEY,
        201);

    doDelete("archive the resource", "QuestionnaireResponse/" + TEST_ID, 204);

    var archivedResponse =
        doInternalGet("archive/r4/QuestionnaireResponse/" + TEST_ID, CLIENT_KEY, 200)
            .expectValid(QuestionnaireResponse.class);

    assertThat(archivedResponse.status()).isEqualTo(QuestionnaireResponse.Status.completed);

    questionnaireResponse.status(QuestionnaireResponse.Status.in_progress);

    doInternalPost(
        "create resource with known ID",
        "r4/QuestionnaireResponse",
        questionnaireResponse,
        CLIENT_KEY,
        201);

    doDelete("archive the resource", "QuestionnaireResponse/" + TEST_ID, 204);

    archivedResponse =
        doInternalGet("archive/r4/QuestionnaireResponse/" + TEST_ID, CLIENT_KEY, 200)
            .expectValid(QuestionnaireResponse.class);

    assertThat(archivedResponse.status()).isEqualTo(QuestionnaireResponse.Status.in_progress);

    doGet("application/json", "QuestionnaireResponse/" + TEST_ID, 404);
  }
}
