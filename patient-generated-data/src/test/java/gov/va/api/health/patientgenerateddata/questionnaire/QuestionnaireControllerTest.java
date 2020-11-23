package gov.va.api.health.patientgenerateddata.questionnaire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.ReferenceQualifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;

public class QuestionnaireControllerTest {
  @Test
  void initDirectFieldAccess() {
    new QuestionnaireController(mock(QuestionnaireRepository.class), mock(ReferenceQualifier.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  @SneakyThrows
  void qualifyReferences() {
    Questionnaire q =
        Questionnaire.builder()
            .id("x")
            .item(
                List.of(
                    Questionnaire.Item.builder()
                        .answerOption(
                            List.of(
                                Questionnaire.AnswerOption.builder()
                                    .valueReference(
                                        Reference.builder().reference("Whatever/123").build())
                                    .initialSelected(false)
                                    .build()))
                        .initial(
                            List.of(
                                Questionnaire.Initial.builder()
                                    .valueReference(
                                        Reference.builder()
                                            .reference("http://foo.bar/Whatever/123")
                                            .build())
                                    .build()))
                        .item(
                            List.of(
                                Questionnaire.Item.builder()
                                    .enableWhen(
                                        List.of(
                                            Questionnaire.EnableWhen.builder()
                                                .answerReference(
                                                    Reference.builder()
                                                        .reference("/Whatever/123")
                                                        .build())
                                                .build()))
                                    .build()))
                        .build()))
            .build();

    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    ReferenceQualifier qualifier = new ReferenceQualifier("http://foo", "r4");
    String payload = JacksonConfig.createMapper().writeValueAsString(q);
    when(repo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").payload(payload).build()));
    System.out.println(
        JacksonConfig.createMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(new QuestionnaireController(repo, qualifier).read("x")));
  }

  @Test
  @SneakyThrows
  void read() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    String payload =
        JacksonConfig.createMapper().writeValueAsString(Questionnaire.builder().id("x").build());
    when(repo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").payload(payload).build()));
    assertThat(new QuestionnaireController(repo, qualifier).read("x"))
        .isEqualTo(Questionnaire.builder().id("x").build());
  }

  @Test
  void read_notFound() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    assertThrows(
        Exceptions.NotFound.class,
        () -> new QuestionnaireController(repo, qualifier).read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    Questionnaire questionnaire = Questionnaire.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaire);
    when(repo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").payload(payload).build()));
    assertThat(new QuestionnaireController(repo, qualifier).update("x", questionnaire))
        .isEqualTo(ResponseEntity.ok().build());
    verify(repo, times(1)).save(QuestionnaireEntity.builder().id("x").payload(payload).build());
  }

  @Test
  @SneakyThrows
  void update_new() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    Questionnaire questionnaire = Questionnaire.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaire);
    assertThat(new QuestionnaireController(repo, qualifier).update("x", questionnaire))
        .isEqualTo(ResponseEntity.status(HttpStatus.CREATED).build());
    verify(repo, times(1)).save(QuestionnaireEntity.builder().id("x").payload(payload).build());
  }
}
