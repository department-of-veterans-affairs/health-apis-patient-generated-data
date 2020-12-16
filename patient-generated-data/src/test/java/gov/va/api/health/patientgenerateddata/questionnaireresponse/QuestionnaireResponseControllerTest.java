package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.DataBinder;
import org.springframework.web.util.UriComponentsBuilder;

public class QuestionnaireResponseControllerTest {

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

  @Test
  @SneakyThrows
  void read() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    String payload =
        JacksonConfig.createMapper()
            .writeValueAsString(QuestionnaireResponse.builder().id("x").build());
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
    Reference ref = Reference.builder().id("1011537977V693883").build();
    QuestionnaireResponse questionnaireResponse =
        QuestionnaireResponse.builder().id("x").author(ref).build();
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaireResponse);
    when(repo.findById("x"))
        .thenReturn(
            Optional.of(QuestionnaireResponseEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new QuestionnaireResponseController(pageLinks, repo).update("x", questionnaireResponse))
        .isEqualTo(ResponseEntity.ok().build());
    verify(repo, times(1))
        .save(QuestionnaireResponseEntity.builder().id("x").payload(payload).build());
  }

  @Test
  @SneakyThrows
  void update_new() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    Reference ref = Reference.builder().id("1011537977V693883").build();
    QuestionnaireResponse questionnaireResponse =
        QuestionnaireResponse.builder().id("x").author(ref).build();
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaireResponse);
    assertThat(
            new QuestionnaireResponseController(pageLinks, repo).update("x", questionnaireResponse))
        .isEqualTo(ResponseEntity.status(HttpStatus.CREATED).build());
    verify(repo, times(1))
        .save(QuestionnaireResponseEntity.builder().id("x").payload(payload).build());
  }

  @ParameterizedTest
  @ValueSource(strings = {"?_id=1", "?author=1011537977V693883"})
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
