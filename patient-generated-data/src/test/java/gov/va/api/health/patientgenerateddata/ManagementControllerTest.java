package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.observation.ObservationSamples.observation;
import static gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireSamples.questionnaire;
import static gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseSamples.questionnaireResponse;
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
        pageLinks, observationRepo, questionnaireRepo, questionnaireResponseRepo);
  }

  static Stream<String> invalid_formats_strings() {
    return Stream.of("", " ", null);
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
