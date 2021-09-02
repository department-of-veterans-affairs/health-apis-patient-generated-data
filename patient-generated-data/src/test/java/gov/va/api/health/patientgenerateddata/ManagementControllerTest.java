package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.observation.Samples.observation;
import static gov.va.api.health.patientgenerateddata.observation.Samples.observationWithLastUpdatedAndSource;
import static gov.va.api.health.patientgenerateddata.questionnaire.Samples.questionnaire;
import static gov.va.api.health.patientgenerateddata.questionnaire.Samples.questionnaireWithLastUpdatedAndSource;
import static gov.va.api.health.patientgenerateddata.questionnaireresponse.Samples.questionnaireResponse;
import static gov.va.api.health.patientgenerateddata.questionnaireresponse.Samples.questionnaireResponseWithLastUpdatedAndSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.patientgenerateddata.observation.ObservationController;
import gov.va.api.health.patientgenerateddata.observation.ObservationEntity;
import gov.va.api.health.patientgenerateddata.observation.ObservationRepository;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireController;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireEntity;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseController;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseEntity;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

public class ManagementControllerTest {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  LinkProperties linkProperties =
      LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();

  Sourcerer sourcerer = new Sourcerer("{}", "sat");

  ObservationRepository observationRepo = mock(ObservationRepository.class);

  QuestionnaireRepository questionnaireRepo = mock(QuestionnaireRepository.class);

  QuestionnaireResponseRepository questionnaireResponseRepo =
      mock(QuestionnaireResponseRepository.class);

  static Stream<String> invalid_formats_strings() {
    return Stream.of("", " ", null);
  }

  private ManagementController _controller() {
    return new ManagementController(
        new ObservationController(linkProperties, observationRepo, sourcerer),
        new QuestionnaireController(linkProperties, questionnaireRepo, sourcerer),
        new QuestionnaireResponseController(linkProperties, questionnaireResponseRepo, sourcerer));
  }

  @Test
  void create_observation() {
    Instant time = Instant.parse("2021-01-01T01:00:00.001Z");
    assertThat(_controller().create(observation(), "Bearer sat", time))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Observation/x"))
                .body(
                    observationWithLastUpdatedAndSource(
                        time, "https://api.va.gov/services/pgd/static-access")));
  }

  @Test
  void create_questionnaire() {
    Instant time = Instant.parse("2021-01-01T01:00:00.001Z");
    assertThat(_controller().create(questionnaire(), "Bearer sat", time))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Questionnaire/x"))
                .body(
                    questionnaireWithLastUpdatedAndSource(
                        time, "https://api.va.gov/services/pgd/static-access")));
  }

  @Test
  void create_questionnaireResponse() {
    Instant time = Instant.parse("2021-01-01T01:00:00.001Z");
    assertThat(_controller().create(questionnaireResponse(), "Bearer sat", time))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/QuestionnaireResponse/x"))
                .body(
                    questionnaireResponseWithLastUpdatedAndSource(
                        time, "https://api.va.gov/services/pgd/static-access")));
  }

  @Test
  @SneakyThrows
  void create_questionnaire_duplicate() {
    when(questionnaireRepo.findById("x"))
        .thenReturn(
            Optional.of(
                QuestionnaireEntity.builder()
                    .id("x")
                    .payload(MAPPER.writeValueAsString(questionnaire()))
                    .build()));
    assertThrows(
        Exceptions.AlreadyExists.class, () -> _controller().create(questionnaire(), "Bearer sat"));
  }

  @ParameterizedTest
  @MethodSource("invalid_formats_strings")
  void invalid_formats(String id) {
    assertThrows(
        Exceptions.BadRequest.class, () -> _controller().create(questionnaire(id), "Bearer sat"));
  }

  @Test
  @SneakyThrows
  void observationIds() {
    when(observationRepo.findAll())
        .thenReturn(
            List.of(
                ObservationEntity.builder()
                    .id("x1")
                    .payload(MAPPER.writeValueAsString(observation()))
                    .build(),
                ObservationEntity.builder()
                    .id("x2")
                    .payload(MAPPER.writeValueAsString(observation()))
                    .build(),
                ObservationEntity.builder()
                    .id("x3")
                    .payload(MAPPER.writeValueAsString(observation()))
                    .build()));
    assertThat(_controller().observationIds()).isEqualTo(List.of("x1", "x2", "x3"));
  }

  @Test
  @SneakyThrows
  void questionnaireIds() {
    when(questionnaireRepo.findAll())
        .thenReturn(
            List.of(
                QuestionnaireEntity.builder()
                    .id("x1")
                    .payload(MAPPER.writeValueAsString(observation()))
                    .build(),
                QuestionnaireEntity.builder()
                    .id("x2")
                    .payload(MAPPER.writeValueAsString(observation()))
                    .build(),
                QuestionnaireEntity.builder()
                    .id("x3")
                    .payload(MAPPER.writeValueAsString(observation()))
                    .build()));
    assertThat(_controller().questionnaireIds()).isEqualTo(List.of("x1", "x2", "x3"));
  }

  @Test
  @SneakyThrows
  void questionnaireResponseIds() {
    when(questionnaireResponseRepo.findAll())
        .thenReturn(
            List.of(
                QuestionnaireResponseEntity.builder()
                    .id("x1")
                    .payload(MAPPER.writeValueAsString(observation()))
                    .build(),
                QuestionnaireResponseEntity.builder()
                    .id("x2")
                    .payload(MAPPER.writeValueAsString(observation()))
                    .build(),
                QuestionnaireResponseEntity.builder()
                    .id("x3")
                    .payload(MAPPER.writeValueAsString(observation()))
                    .build()));
    assertThat(_controller().questionnaireResponseIds()).isEqualTo(List.of("x1", "x2", "x3"));
  }
}
