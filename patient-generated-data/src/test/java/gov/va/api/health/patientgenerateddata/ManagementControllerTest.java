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
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

public class ManagementControllerTest {
  private static ManagementController controller() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    var observationRepo = mock(ObservationRepository.class);
    var patientRepo = mock(PatientRepository.class);
    var questionnaireRepo = mock(QuestionnaireRepository.class);
    var questionnaireResponseRepo = mock(QuestionnaireResponseRepository.class);
    return new ManagementController(
        pageLinks, observationRepo, patientRepo, questionnaireRepo, questionnaireResponseRepo);
  }

  static Stream<String> invalid_formats_strings() {
    return Stream.of("", " ", null);
  }

  private static Identifier mpi(String icn) {
    return Identifier.builder()
        .use(Identifier.IdentifierUse.usual)
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

  private static Observation observation() {
    return Observation.builder().status(Observation.ObservationStatus.unknown).build();
  }

  private static Patient patient(String icn) {
    return Patient.builder().id(icn).identifier(new ArrayList<>(List.of(mpi(icn)))).build();
  }

  private static Questionnaire questionnaire(String id) {
    return Questionnaire.builder()
        .id(id)
        .title("x")
        .status(Questionnaire.PublicationStatus.active)
        .build();
  }

  private static QuestionnaireResponse questionnaireResponse() {
    return QuestionnaireResponse.builder().status(QuestionnaireResponse.Status.completed).build();
  }

  @Test
  void create_observation() {
    var observation = observation().id("x");
    assertThat(controller().create(observation))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Observation/x"))
                .body(observation));
  }

  @Test
  void create_patient() {
    var patient = patient("9999999999V999999");
    assertThat(controller().create(patient))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Patient/9999999999V999999"))
                .body(patient));
  }

  @Test
  void create_questionnaire() {
    var questionnaire = questionnaire("x");
    assertThat(controller().create(questionnaire))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Questionnaire/x"))
                .body(questionnaire));
  }

  @Test
  void create_questionnaire_duplicate() {
    var controller = controller();
    var questionnaire = questionnaire("x");
    when(controller.questionnaireRepository().existsById("x")).thenReturn(true);
    assertThrows(Exceptions.AlreadyExists.class, () -> controller.create(questionnaire));
  }

  @Test
  void create_questionnaire_response() {
    var questionnaireResponse = questionnaireResponse().id("x");
    assertThat(controller().create(questionnaireResponse))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/QuestionnaireResponse/x"))
                .body(questionnaireResponse));
  }

  @ParameterizedTest
  @MethodSource("invalid_formats_strings")
  void invalid_formats(String id) {
    var controller = controller();
    var questionnaire = questionnaire(id);
    assertThrows(Exceptions.BadRequest.class, () -> controller.create(questionnaire));
  }
}
