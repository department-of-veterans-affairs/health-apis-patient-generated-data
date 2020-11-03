package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class QuestionnaireControllerTest {
  @Test
  void badPayload() {
    assertThrows(
        Exceptions.InvalidPayload.class,
        () -> QuestionnaireController.deserializedPayload("x", "notjson", Questionnaire.class));
  }

  @Test
  @SneakyThrows
  void read() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    String payload =
        JacksonConfig.createMapper().writeValueAsString(Questionnaire.builder().id("x").build());
    when(repo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").payload(payload).build()));
    assertThat(new QuestionnaireController(repo).read("x"))
        .isEqualTo(Questionnaire.builder().id("x").build());
  }

  @Test
  void read_notFound() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    assertThrows(
        Exceptions.NotFound.class, () -> new QuestionnaireController(repo).read("notfound"));
  }
}
