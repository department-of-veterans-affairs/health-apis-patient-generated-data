package gov.va.api.health.patientgenerateddata.questionnaireresponse;

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
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse.Status;
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

public class QuestionnaireResponseControllerTest {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  LinkProperties pageLinks =
      LinkProperties.builder()
          .defaultPageSize(500)
          .maxPageSize(20)
          .baseUrl("http://foo.com")
          .r4BasePath("r4")
          .build();

  private static MockHttpServletRequest requestFromUri(String uri) {
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

  private QuestionnaireResponseController controller() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    return controller(repo);
  }

  private QuestionnaireResponseController controller(QuestionnaireResponseRepository repo) {
    return new QuestionnaireResponseController(pageLinks, repo);
  }

  @Test
  @SneakyThrows
  void create() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    QuestionnaireResponseController controller =
        new QuestionnaireResponseController(pageLinks, repo);
    var questionnaireResponse = questionnaireResponse();
    var questionnaireResponseWithId = questionnaireResponse().id("123");
    var persisted = MAPPER.writeValueAsString(questionnaireResponseWithId);
    assertThat(controller.create("123", questionnaireResponse))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/QuestionnaireResponse/123"))
                .body(questionnaireResponseWithId));
    verify(repo, times(1))
        .save(QuestionnaireResponseEntity.builder().id("123").payload(persisted).build());
  }

  @Test
  @SneakyThrows
  void create_invalid() {
    var questionnaireResponse = questionnaireResponse().id("123");
    var repo = mock(QuestionnaireResponseRepository.class);
    var pageLinks = mock(LinkProperties.class);
    var controller = new QuestionnaireResponseController(pageLinks, repo);
    assertThrows(Exceptions.BadRequest.class, () -> controller.create(questionnaireResponse));
  }

  @Test
  void initDirectFieldAccess() {
    new QuestionnaireResponseController(
            mock(LinkProperties.class), mock(QuestionnaireResponseRepository.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "?_id=123&authored=2000-01-01T00:00:00Z"})
  @SneakyThrows
  void invalidRequests(String query) {
    var r = requestFromUri("http://fonzy.com/r4/QuestionnaireResponse" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(r));
  }

  private QuestionnaireResponse questionnaireResponse() {
    return QuestionnaireResponse.builder().status(Status.completed).build();
  }

  @Test
  @SneakyThrows
  void read() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    String payload = MAPPER.writeValueAsString(QuestionnaireResponse.builder().id("x").build());
    when(repo.findById("x"))
        .thenReturn(
            Optional.of(QuestionnaireResponseEntity.builder().id("x").payload(payload).build()));
    assertThat(new QuestionnaireResponseController(pageLinks, repo).read("x"))
        .isEqualTo(QuestionnaireResponse.builder().id("x").build());
  }

  @Test
  void read_notFound() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    assertThrows(
        Exceptions.NotFound.class,
        () -> new QuestionnaireResponseController(pageLinks, repo).read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    Reference authorRef = Reference.builder().reference("1011537977V693883").build();
    QuestionnaireResponse questionnaireResponse =
        QuestionnaireResponse.builder().id("x").author(authorRef).build();
    String payload = MAPPER.writeValueAsString(questionnaireResponse);
    when(repo.findById("x"))
        .thenReturn(
            Optional.of(QuestionnaireResponseEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new QuestionnaireResponseController(pageLinks, repo).update("x", questionnaireResponse))
        .isEqualTo(ResponseEntity.ok(questionnaireResponse));
    verify(repo, times(1))
        .save(QuestionnaireResponseEntity.builder().id("x").payload(payload).build());
  }

  @Test
  @SneakyThrows
  void update_not_existing() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    Reference authorRef = Reference.builder().reference("1011537977V693883").build();
    QuestionnaireResponse questionnaireResponse =
        QuestionnaireResponse.builder().id("x").author(authorRef).build();
    assertThrows(
        Exceptions.NotFound.class,
        () ->
            new QuestionnaireResponseController(pageLinks, repo)
                .update("x", questionnaireResponse));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=1",
        "?author=1011537977V693883",
        "?questionnaire=37953b72-961b-41ee-bd05-86c62bacc46b"
      })
  @SneakyThrows
  void validSearch(String query) {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    QuestionnaireResponseController controller = controller(repo);
    var anySpec = ArgumentMatchers.<Specification<QuestionnaireResponseEntity>>any();
    when(repo.findAll(anySpec, any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl<QuestionnaireResponseEntity>(
                    List.of(
                        QuestionnaireResponseEntity.builder()
                            .build()
                            .id("1")
                            .payload("{ \"id\": 1}")),
                    i.getArgument(1, Pageable.class),
                    1));
    var r = requestFromUri("http://fonzy.com/r4/QuestionnaireResponse" + query);
    var actual = controller.search(r);
    assertThat(actual.entry()).hasSize(1);
  }
}
