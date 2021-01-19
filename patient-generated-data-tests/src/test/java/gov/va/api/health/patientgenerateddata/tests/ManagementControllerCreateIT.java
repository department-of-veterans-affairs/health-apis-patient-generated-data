package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalPost;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.CLIENT_KEY_DEFAULT;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Patient.Gender;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.Questionnaire.PublicationStatus;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse.Status;
import gov.va.api.health.sentinel.Environment;
import java.util.List;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

public class ManagementControllerCreateIT {
  private static final String CLIENT_KEY = System.getProperty("client-key", CLIENT_KEY_DEFAULT);

  public static Questionnaire _questionnaire(String id) {
    return Questionnaire.builder().id(id).title("x").status(PublicationStatus.active).build();
  }

  @BeforeAll
  static void assumeEnvironment() {
    // These tests invent data that will not be cleaned up
    // To avoid polluting the database, they should only run locally
    assumeEnvironmentIn(Environment.LOCAL);
  }

  private Observation _observation(String id) {
    return ObservationCreateIT.observation().id(id);
  }

  private Patient _patient(String icn) {
    return Patient.builder()
        .resourceType("Patient")
        .id(icn)
        .identifier(List.of(Identifier.builder().value(icn).build()))
        .name(List.of(HumanName.builder().text("Test McTest").build()))
        .gender(Gender.unknown)
        .build();
  }

  private QuestionnaireResponse _questionnaireResponse(String id) {
    return QuestionnaireResponse.builder().id(id).status(Status.completed).build();
  }

  @Test
  public void create_observation() {
    var observation = _observation("it-internal-observation");
    doInternalPost("Observation", observation, "create resource", 201, CLIENT_KEY);
  }

  @Test
  public void create_patient() {
    // ID must be in MPI format!
    var patient = _patient("it-internal-patient");
    doInternalPost("Patient", patient, "create resource", 201, CLIENT_KEY);
  }

  @Test
  public void create_questionnaire() {
    var questionnaire = _questionnaire("it-internal-questionnaire");
    doInternalPost("Questionnaire", questionnaire, "create resource", 201, CLIENT_KEY);
  }

  @Test
  public void create_questionnaire_response() {
    var questionnaireResponse = _questionnaireResponse("it-internal-questionnaire-response");
    doInternalPost(
        "QuestionnaireResponse", questionnaireResponse, "create resource", 201, CLIENT_KEY);
  }
}
