package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.patientgenerateddata.observation.ObservationRepository;
import gov.va.api.health.patientgenerateddata.patient.PatientRepository;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Observation.ObservationStatus;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.Questionnaire.PublicationStatus;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

public class ManagementControllerTest {
  private static Identifier _mpi(String icn) {
    return Identifier.builder()
        .use(IdentifierUse.usual)
        .type(
            CodeableConcept.builder()
                .coding(
                    Collections.singletonList(
                        Coding.builder().system("http://hl7.org/fhir/v2/0203").code("MR").build()))
                .build())
        .system("http://va.gov/mpi")
        .value(icn)
        .build();
  }

  static Stream<String> invalid_formats_strings() {
    return Stream.of("", " ", null);
  }

  private ManagementController _controller() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    var observationRepo = mock(ObservationRepository.class);
    var patientRepo = mock(PatientRepository.class);
    var questionnaireRepo = mock(QuestionnaireRepository.class);
    var questionnaireResponseRepo = mock(QuestionnaireResponseRepository.class);
    return new ManagementController(
        pageLinks, observationRepo, patientRepo, questionnaireRepo, questionnaireResponseRepo);
  }

  private Observation _observation() {
    return Observation.builder().status(ObservationStatus.unknown).build();
  }

  private Patient _patient(String icn) {
    return Patient.builder().id(icn).identifier(new ArrayList<>(List.of(_mpi(icn)))).build();
  }

  private Questionnaire _questionnaire(String id) {
    return Questionnaire.builder().id(id).title("x").status(PublicationStatus.active).build();
  }

  private QuestionnaireResponse _questionnaireResponse() {
    return QuestionnaireResponse.builder().status(Status.completed).build();
  }

  @Test
  public void create_observation() {
    var observation = _observation().id("x");
    assertThat(_controller().create(observation))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Observation/x"))
                .body(observation));
  }

  @Test
  public void create_patient() {
    var patient = _patient("9999999999V999999");
    assertThat(_controller().create(patient))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Patient/9999999999V999999"))
                .body(patient));
  }

  @Test
  public void create_questionnaire() {
    var questionnaire = _questionnaire("x");
    assertThat(_controller().create(questionnaire))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Questionnaire/x"))
                .body(questionnaire));
  }

  @Test
  public void create_questionnaire_duplicate() {
    var controller = _controller();
    var questionnaire = _questionnaire("x");
    when(controller.questionnaireRepository().existsById("x")).thenReturn(true);
    assertThrows(Exceptions.AlreadyExists.class, () -> controller.create(questionnaire));
  }

  @Test
  public void create_questionnaire_response() {
    var questionnaireResponse = _questionnaireResponse().id("x");
    assertThat(_controller().create(questionnaireResponse))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/QuestionnaireResponse/x"))
                .body(questionnaireResponse));
  }

  @ParameterizedTest
  @MethodSource("invalid_formats_strings")
  public void invalid_formats(String id) {
    var controller = _controller();
    var questionnaire = _questionnaire(id);
    assertThrows(Exceptions.BadRequest.class, () -> controller.create(questionnaire));
  }
}
