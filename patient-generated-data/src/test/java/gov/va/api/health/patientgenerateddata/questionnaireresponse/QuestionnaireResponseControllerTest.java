package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.ReferenceQualifier;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;

public class QuestionnaireResponseControllerTest {
  @Test
  void initDirectFieldAccess() {
    new QuestionnaireResponseController(
            mock(QuestionnaireResponseRepository.class), mock(ReferenceQualifier.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  @SneakyThrows
  void read() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    String payload =
        JacksonConfig.createMapper()
            .writeValueAsString(QuestionnaireResponse.builder().id("x").build());
    when(repo.findById("x"))
        .thenReturn(
            Optional.of(QuestionnaireResponseEntity.builder().id("x").payload(payload).build()));
    assertThat(new QuestionnaireResponseController(repo, qualifier).read("x"))
        .isEqualTo(QuestionnaireResponse.builder().id("x").build());
  }

  @Test
  void read_notFound() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    assertThrows(
        Exceptions.NotFound.class,
        () -> new QuestionnaireResponseController(repo, qualifier).read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    QuestionnaireResponse questionnaireResponse = QuestionnaireResponse.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaireResponse);
    when(repo.findById("x"))
        .thenReturn(
            Optional.of(QuestionnaireResponseEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new QuestionnaireResponseController(repo, qualifier).update("x", questionnaireResponse))
        .isEqualTo(ResponseEntity.ok().build());
    verify(repo, times(1))
        .save(QuestionnaireResponseEntity.builder().id("x").payload(payload).build());
  }

  @Test
  @SneakyThrows
  void update_new() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    QuestionnaireResponse questionnaireResponse = QuestionnaireResponse.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaireResponse);
    assertThat(
            new QuestionnaireResponseController(repo, qualifier).update("x", questionnaireResponse))
        .isEqualTo(ResponseEntity.status(HttpStatus.CREATED).build());
    verify(repo, times(1))
        .save(QuestionnaireResponseEntity.builder().id("x").payload(payload).build());
  }
}
