package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.observation.Samples.observation;
import static gov.va.api.health.patientgenerateddata.questionnaire.Samples.questionnaire;
import static gov.va.api.health.patientgenerateddata.questionnaireresponse.Samples.questionnaireResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.patientgenerateddata.observation.ObservationController;
import gov.va.api.health.patientgenerateddata.observation.ObservationRepository;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireController;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireEntity;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseController;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

public class ManagementControllerTest {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  static LinkProperties linkProperties =
      LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();

  static Sourcerer sourcerer = new Sourcerer("{}", "sat");

  private static ManagementController controller() {
    return controller(
        mock(ObservationRepository.class),
        mock(QuestionnaireRepository.class),
        mock(QuestionnaireResponseRepository.class));
  }

  private static ManagementController controller(
      ObservationRepository oRepo,
      QuestionnaireRepository qRepo,
      QuestionnaireResponseRepository qrRepo) {
    return new ManagementController(
        new ObservationController(linkProperties, oRepo, sourcerer),
        new QuestionnaireController(linkProperties, qRepo, sourcerer),
        new QuestionnaireResponseController(linkProperties, qrRepo, sourcerer));
  }

  static Stream<String> invalid_formats_strings() {
    return Stream.of("", " ", null);
  }

  @Test
  void create_observation() {
    var observation = observation();
    assertThat(controller().create(observation, "Bearer sat"))
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
  @SneakyThrows
  void create_questionnaire_duplicate() {
    QuestionnaireRepository qRepo = mock(QuestionnaireRepository.class);
    when(qRepo.findById("x"))
        .thenReturn(
            Optional.of(
                QuestionnaireEntity.builder()
                    .id("x")
                    .payload(MAPPER.writeValueAsString(questionnaire()))
                    .build()));
    ManagementController controller =
        controller(
            mock(ObservationRepository.class), qRepo, mock(QuestionnaireResponseRepository.class));
    assertThrows(Exceptions.AlreadyExists.class, () -> controller.create(questionnaire()));
  }

  @ParameterizedTest
  @MethodSource("invalid_formats_strings")
  void invalid_formats(String id) {
    var questionnaire = questionnaire(id);
    assertThrows(Exceptions.BadRequest.class, () -> controller().create(questionnaire));
  }
}
