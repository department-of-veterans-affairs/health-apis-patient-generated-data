package gov.va.api.health.patientgenerateddata.questionnaire;

import static gov.va.api.health.patientgenerateddata.MockRequests.requestFromUri;
import static gov.va.api.health.patientgenerateddata.questionnaire.Samples.questionnaire;
import static gov.va.api.health.patientgenerateddata.questionnaire.Samples.questionnaireWithLastUpdatedAndSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.JacksonMapperConfig;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.patientgenerateddata.Sourcerer;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;

public class QuestionnaireControllerTest {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  @Test
  @SneakyThrows
  void create() {
    Instant time = Instant.parse("2021-01-01T01:00:00.001Z");
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    QuestionnaireController controller =
        new QuestionnaireController(pageLinks, repo, new Sourcerer("{}", "sat"));
    var questionnaire = questionnaire();
    var persisted = MAPPER.writeValueAsString(questionnaire);
    assertThat(controller.create(questionnaire, "Bearer sat", time))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Questionnaire/x"))
                .body(
                    questionnaireWithLastUpdatedAndSource(
                        time, "https://api.va.gov/services/pgd/static-access")));
    verify(repo, times(1)).save(QuestionnaireEntity.builder().id("x").payload(persisted).build());
  }

  @Test
  void create_invalid() {
    var questionnaire = questionnaire().id("123");
    var repo = mock(QuestionnaireRepository.class);
    var pageLinks = mock(LinkProperties.class);
    var controller = new QuestionnaireController(pageLinks, repo, new Sourcerer("{}", "sat"));
    assertThrows(Exceptions.BadRequest.class, () -> controller.create(questionnaire, ""));
  }

  @Test
  @SneakyThrows
  void getAllIds() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    QuestionnaireController controller =
        new QuestionnaireController(pageLinks, repo, new Sourcerer("{}", "sat"));
    when(repo.findAll())
        .thenReturn(
            List.of(
                QuestionnaireEntity.builder()
                    .id("x1")
                    .payload(MAPPER.writeValueAsString(questionnaire("x1")))
                    .build(),
                QuestionnaireEntity.builder()
                    .id("x2")
                    .payload(MAPPER.writeValueAsString(questionnaire("x2")))
                    .build(),
                QuestionnaireEntity.builder()
                    .id("x3")
                    .payload(MAPPER.writeValueAsString(questionnaire("x3")))
                    .build()));
    assertThat(controller.getAllIds()).isEqualTo(List.of("x1", "x2", "x3"));
  }

  @Test
  void initDirectFieldAccess() {
    new QuestionnaireController(
            mock(LinkProperties.class),
            mock(QuestionnaireRepository.class),
            new Sourcerer("{}", "sat"))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  @SneakyThrows
  void read() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    String payload = MAPPER.writeValueAsString(questionnaire());
    when(repo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new QuestionnaireController(
                    mock(LinkProperties.class), repo, new Sourcerer("{}", "sat"))
                .read("x"))
        .isEqualTo(questionnaire());
  }

  @Test
  void read_notFound() {
    assertThrows(
        Exceptions.NotFound.class,
        () ->
            new QuestionnaireController(
                    mock(LinkProperties.class),
                    mock(QuestionnaireRepository.class),
                    new Sourcerer("{}", "sat"))
                .read("notfound"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "?_id=123&context-type-value=x$y", "?_id=123&_lastUpdated=gt2020"})
  void search_invalid(String query) {
    LinkProperties pageLinks =
        LinkProperties.builder()
            .defaultPageSize(1)
            .maxPageSize(1)
            .baseUrl("http://foo.com")
            .r4BasePath("r4")
            .build();
    QuestionnaireController controller =
        new QuestionnaireController(
            pageLinks, mock(QuestionnaireRepository.class), new Sourcerer("{}", "sat"));
    var req = requestFromUri("http://fonzy.com/r4/Questionnaire" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller.search(req));
  }

  @ParameterizedTest
  @ValueSource(strings = {"?_id=1", "?_lastUpdated=gt2020", "?context-type-value=x$y"})
  void search_valid(String query) {
    LinkProperties pageLinks =
        LinkProperties.builder()
            .defaultPageSize(1)
            .maxPageSize(1)
            .baseUrl("http://foo.com")
            .r4BasePath("r4")
            .build();
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    QuestionnaireController controller =
        new QuestionnaireController(pageLinks, repo, new Sourcerer("{}", "sat"));
    var anySpec = ArgumentMatchers.<Specification<QuestionnaireEntity>>any();
    when(repo.findAll(anySpec, any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl<QuestionnaireEntity>(
                    List.of(QuestionnaireEntity.builder().build().id("1").payload("{ \"id\": 1}")),
                    i.getArgument(1, Pageable.class),
                    1));
    var r = requestFromUri("http://fonzy.com/r4/Questionnaire" + query);
    var actual = controller.search(r);
    assertThat(actual.entry()).hasSize(1);
  }

  @Test
  @SneakyThrows
  void update_existing() {
    Instant now = Instant.parse("2021-01-01T01:00:00.001Z");
    Questionnaire questionnaire =
        questionnaireWithLastUpdatedAndSource(now, "https://api.va.gov/services/pgd/static-access");
    String payload = MAPPER.writeValueAsString(questionnaire);
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    when(repo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new QuestionnaireController(
                    mock(LinkProperties.class), repo, new Sourcerer("{}", "sat"))
                .update(questionnaire, "Bearer sat", now))
        .isEqualTo(
            ResponseEntity.ok(
                questionnaireWithLastUpdatedAndSource(
                    now, "https://api.va.gov/services/pgd/static-access")));
    verify(repo, times(1)).save(QuestionnaireEntity.builder().id("x").payload(payload).build());
  }

  @Test
  void update_not_existing() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    assertThrows(
        Exceptions.NotFound.class,
        () ->
            new QuestionnaireController(
                    pageLinks, mock(QuestionnaireRepository.class), new Sourcerer("{}", "sat"))
                .update("x", questionnaire(), "Bearer sat"));
  }

  @Test
  @SneakyThrows
  void update_payloadSource() {
    Instant now = Instant.parse("2021-01-01T01:00:00.001Z");
    Questionnaire questionnaire =
        questionnaireWithLastUpdatedAndSource(
            now, "https://api.va.gov/services/pgd/zombie-bob-nelson");
    Questionnaire staticQuestionnaireSource =
        questionnaireWithLastUpdatedAndSource(now, "https://api.va.gov/services/pgd/static-access");
    String payload = MAPPER.writeValueAsString(staticQuestionnaireSource);
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    when(repo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new QuestionnaireController(
                    mock(LinkProperties.class), repo, new Sourcerer("{}", "sat"))
                .update(questionnaire, "Bearer sat", now))
        .isEqualTo(
            ResponseEntity.ok(
                questionnaireWithLastUpdatedAndSource(
                    now, "https://api.va.gov/services/pgd/static-access")));
    verify(repo, times(1)).save(QuestionnaireEntity.builder().id("x").payload(payload).build());
  }
}
