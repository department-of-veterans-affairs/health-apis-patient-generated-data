package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.UrlPageLinks;
import gov.va.api.health.patientgenerateddata.vulcanizer.LinkProperties;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.DataBinder;
import org.springframework.web.util.UriComponentsBuilder;

public class QuestionnaireResponseControllerTest {
  LinkProperties mockLinkProperties = mock(LinkProperties.class);

  UrlPageLinks pageLinks =
      UrlPageLinks.builder().baseUrl("http://foo.com").r4BasePath("r4").build();

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

  @Test
  void initDirectFieldAccess() {
    new QuestionnaireResponseController(
            mock(LinkProperties.class), mock(QuestionnaireResponseRepository.class))
        .initDirectFieldAccess(mock(DataBinder.class));
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
    assertThat(new QuestionnaireResponseController(mockLinkProperties, repo).read("x"))
        .isEqualTo(QuestionnaireResponse.builder().id("x").build());
  }

  @Test
  void read_notFound() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    assertThrows(
        Exceptions.NotFound.class,
        () -> new QuestionnaireResponseController(mockLinkProperties, repo).read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    QuestionnaireResponse questionnaireResponse = QuestionnaireResponse.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaireResponse);
    when(repo.findById("x"))
        .thenReturn(
            Optional.of(QuestionnaireResponseEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new QuestionnaireResponseController(mockLinkProperties, repo)
                .update("x", questionnaireResponse))
        .isEqualTo(ResponseEntity.ok().build());
    verify(repo, times(1))
        .save(QuestionnaireResponseEntity.builder().id("x").payload(payload).build());
  }

  @Test
  @SneakyThrows
  void update_new() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    QuestionnaireResponse questionnaireResponse = QuestionnaireResponse.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaireResponse);
    assertThat(
            new QuestionnaireResponseController(mockLinkProperties, repo)
                .update("x", questionnaireResponse))
        .isEqualTo(ResponseEntity.status(HttpStatus.CREATED).build());
    verify(repo, times(1))
        .save(QuestionnaireResponseEntity.builder().id("x").payload(payload).build());
  }

  @ParameterizedTest
  @ValueSource(strings = {"?_id=1"})
  @SneakyThrows
  void validSearch(String query) {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    QuestionnaireResponseController controller =
        new QuestionnaireResponseController(
            LinkProperties.builder().urlPageLinks(pageLinks).build(), repo);
    when(repo.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
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
