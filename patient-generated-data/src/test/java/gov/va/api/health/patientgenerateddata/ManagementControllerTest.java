package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.observation.Samples.observation;
import static gov.va.api.health.patientgenerateddata.questionnaire.Samples.questionnaire;
import static gov.va.api.health.patientgenerateddata.questionnaireresponse.Samples.questionnaireResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.patientgenerateddata.observation.ObservationRepository;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import java.net.URI;
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
    var questionnaireRepo = mock(QuestionnaireRepository.class);
    var questionnaireResponseRepo = mock(QuestionnaireResponseRepository.class);
    return new ManagementController(
        pageLinks,
        new ClientIdMajig("{}"),
        observationRepo,
        questionnaireRepo,
        questionnaireResponseRepo);
  }

  static Stream<String> invalid_formats_strings() {
    return Stream.of("", " ", null);
  }

  @Test
  void create_observation() {
    var observation = observation();
    assertThat(controller().create(observation))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Observation/x"))
                .body(observation));
  }

  @Test
  void create_questionnaire() {
    var questionnaire = questionnaire();
    assertThat(controller().create(questionnaire))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Questionnaire/x"))
                .body(questionnaire));
  }

  @Test
  void create_questionnaireResponse() {
    var questionnaireResponse = questionnaireResponse();
    assertThat(controller().create(questionnaireResponse))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/QuestionnaireResponse/x"))
                .body(questionnaireResponse));
  }

  @Test
  void create_questionnaire_duplicate() {
    var questionnaire = questionnaire();
    ManagementController controller = controller();
    when(controller.questionnaireRepository().existsById("x")).thenReturn(true);
    assertThrows(Exceptions.AlreadyExists.class, () -> controller.create(questionnaire));
  }

  @ParameterizedTest
  @MethodSource("invalid_formats_strings")
  void invalid_formats(String id) {
    var questionnaire = questionnaire(id);
    assertThrows(Exceptions.BadRequest.class, () -> controller().create(questionnaire));
  }
}
