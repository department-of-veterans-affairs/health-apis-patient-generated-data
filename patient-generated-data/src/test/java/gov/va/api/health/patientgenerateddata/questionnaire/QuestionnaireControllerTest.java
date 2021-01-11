package gov.va.api.health.patientgenerateddata.questionnaire;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.Questionnaire.PublicationStatus;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import java.net.URI;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.DataBinder;
import org.springframework.web.util.UriComponentsBuilder;

public class QuestionnaireControllerTest {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static MockHttpServletRequest _requestFromUri(String uri) {
    var u = UriComponentsBuilder.fromUriString(uri).build();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(u.getPath());
    request.setRemoteHost(u.getHost());
    request.setProtocol(u.getScheme());
    request.setServerPort(u.getPort());
    u.getQueryParams()
        .entrySet()
        .forEach(e -> request.addParameter(e.getKey(), e.getValue().toArray(new String[0])));
    return request;
  }

  private Questionnaire _questionnaire() {
    return Questionnaire.builder().title("x").status(PublicationStatus.active).build();
  }

  @Test
  @SneakyThrows
  void create() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    QuestionnaireController controller = new QuestionnaireController(pageLinks, repo);
    var questionnaire = _questionnaire();
    var questionnaireWithId = _questionnaire().id("123");
    var persisted = MAPPER.writeValueAsString(questionnaire);
    assertThat(controller.create("123", questionnaire))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Questionnaire/" + 123))
                .body(questionnaireWithId));
    verify(repo, times(1)).save(QuestionnaireEntity.builder().id("123").payload(persisted).build());
  }

  @Test
  @SneakyThrows
  void create_invalid() {
    var questionnaire = _questionnaire().id("123");
    var repo = mock(QuestionnaireRepository.class);
    var pageLinks = mock(LinkProperties.class);
    var controller = new QuestionnaireController(pageLinks, repo);
    assertThrows(Exceptions.BadRequest.class, () -> controller.create(questionnaire));
  }

  @Test
  void initDirectFieldAccess() {
    new QuestionnaireController(mock(LinkProperties.class), mock(QuestionnaireRepository.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  @SneakyThrows
  void read() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    String payload = MAPPER.writeValueAsString(Questionnaire.builder().id("x").build());
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

  @ParameterizedTest
  @ValueSource(strings = {"", "?_id=123&context-type-value=x$y"})
  void search_invalid(String query) {
    LinkProperties pageLinks =
        LinkProperties.builder()
            .defaultPageSize(1)
            .maxPageSize(1)
            .baseUrl("http://foo.com")
            .r4BasePath("r4")
            .build();
    QuestionnaireController controller =
        new QuestionnaireController(pageLinks, mock(QuestionnaireRepository.class));
    var req = _requestFromUri("http://fonzy.com/r4/Questionnaire" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller.search(req));
  }

  @ParameterizedTest
  @ValueSource(strings = {"?_id=1", "?context-type-value=x$y"})
  void search_valid(String query) {
    LinkProperties pageLinks =
        LinkProperties.builder()
            .defaultPageSize(1)
            .maxPageSize(1)
            .baseUrl("http://foo.com")
            .r4BasePath("r4")
            .build();
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    QuestionnaireController controller = new QuestionnaireController(pageLinks, repo);
    var anySpec = ArgumentMatchers.<Specification<QuestionnaireEntity>>any();
    when(repo.findAll(anySpec, any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl<QuestionnaireEntity>(
                    List.of(QuestionnaireEntity.builder().build().id("1").payload("{ \"id\": 1}")),
                    i.getArgument(1, Pageable.class),
                    1));
    var r = _requestFromUri("http://fonzy.com/r4/Questionnaire" + query);
    var actual = controller.search(r);
    assertThat(actual.entry()).hasSize(1);
  }

  @Test
  @SneakyThrows
  void update_existing() {
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    Questionnaire questionnaire = Questionnaire.builder().id("x").build();
    String payload = MAPPER.writeValueAsString(questionnaire);
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
  void update_not_existing() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    QuestionnaireRepository repo = mock(QuestionnaireRepository.class);
    Questionnaire questionnaire = Questionnaire.builder().id("x").build();
    assertThrows(
        Exceptions.NotFound.class,
        () -> new QuestionnaireController(pageLinks, repo).update("x", questionnaire));
  }
}
