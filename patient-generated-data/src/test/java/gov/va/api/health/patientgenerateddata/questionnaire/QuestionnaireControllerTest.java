package gov.va.api.health.patientgenerateddata.questionnaire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.net.URI;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;

public class QuestionnaireControllerTest {
  @Test
  void initDirectFieldAccess() {
    new QuestionnaireController(mock(LinkProperties.class), mock(QuestionnaireRepository.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  @SneakyThrows
  void read() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    String payload =
        JacksonConfig.createMapper().writeValueAsString(Questionnaire.builder().id("x").build());
    when(repo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").payload(payload).build()));
    assertThat(new QuestionnaireController(mock(LinkProperties.class), repo).read("x"))
        .isEqualTo(Questionnaire.builder().id("x").build());
  }

  @Test
  void read_notFound() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    assertThrows(
        Exceptions.NotFound.class,
        () -> new QuestionnaireController(mock(LinkProperties.class), repo).read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    Questionnaire questionnaire = Questionnaire.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaire);
    when(repo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new QuestionnaireController(mock(LinkProperties.class), repo)
                .update("x", questionnaire))
        .isEqualTo(ResponseEntity.ok(questionnaire));
    verify(repo, times(1)).save(QuestionnaireEntity.builder().id("x").payload(payload).build());
  }

  @Test
  @SneakyThrows
  void update_new() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    Questionnaire questionnaire = Questionnaire.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaire);
    assertThat(new QuestionnaireController(pageLinks, repo).update("x", questionnaire))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Questionnaire/x"))
                .body(questionnaire));
    verify(repo, times(1)).save(QuestionnaireEntity.builder().id("x").payload(payload).build());
  }
}
