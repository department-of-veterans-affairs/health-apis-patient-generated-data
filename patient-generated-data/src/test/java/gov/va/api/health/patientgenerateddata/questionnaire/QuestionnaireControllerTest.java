package gov.va.api.health.patientgenerateddata.questionnaire;

import static gov.va.api.health.patientgenerateddata.MockRequests.requestFromUri;
import static gov.va.api.health.patientgenerateddata.questionnaire.Samples.questionnaire;
import static gov.va.api.health.patientgenerateddata.questionnaire.Samples.questionnaireWithLastUpdated;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.patientgenerateddata.Sourcerer;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.JacksonMapperConfig;
import gov.va.api.health.patientgenerateddata.LinkProperties;
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
        new QuestionnaireController(pageLinks, repo, new Sourcerer("{}", ""));
    var questionnaire = questionnaire().id("x");
    var persisted = MAPPER.writeValueAsString(questionnaire);
    assertThat(controller.create(questionnaire, time))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Questionnaire/x"))
                .body(questionnaireWithLastUpdated(time)));
    verify(repo, times(1)).save(QuestionnaireEntity.builder().id("x").payload(persisted).build());
  }

  @Test
  void create_invalid() {
    var questionnaire = questionnaire().id("123");
    var repo = mock(QuestionnaireRepository.class);
    var pageLinks = mock(LinkProperties.class);
    var controller = new QuestionnaireController(pageLinks, repo, new Sourcerer("{}", ""));
    assertThrows(Exceptions.BadRequest.class, () -> controller.create(questionnaire));
  }

  @Test
  void initDirectFieldAccess() {
    new QuestionnaireController(
            mock(LinkProperties.class),
            mock(QuestionnaireRepository.class),
            new Sourcerer("{}", ""))
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
            new QuestionnaireController(mock(LinkProperties.class), repo, new Sourcerer("{}", ""))
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
                    new Sourcerer("{}", ""))
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
            pageLinks, mock(QuestionnaireRepository.class), new Sourcerer("{}", ""));
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
        new QuestionnaireController(pageLinks, repo, new Sourcerer("{}", ""));
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
    Questionnaire questionnaire = questionnaire();
    String payload = MAPPER.writeValueAsString(questionnaire);
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    when(repo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new QuestionnaireController(mock(LinkProperties.class), repo, new Sourcerer("{}", ""))
                .update(questionnaire, now))
        .isEqualTo(ResponseEntity.ok(questionnaireWithLastUpdated(now)));
    verify(repo, times(1)).save(QuestionnaireEntity.builder().id("x").payload(payload).build());
  }

  @Test
  void update_not_existing() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    Questionnaire questionnaire = questionnaire("x");
    assertThrows(
        Exceptions.NotFound.class,
        () ->
            new QuestionnaireController(pageLinks, repo, new Sourcerer("{}", ""))
                .update("x", questionnaire));
  }
}
