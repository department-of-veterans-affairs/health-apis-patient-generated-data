package gov.va.api.health.patientgenerateddata.questionnaire;

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
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseController;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseEntity;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
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
    var persisted = JacksonConfig.createMapper().writeValueAsString(questionnaire);
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
    var r = _requestFromUri("http://fonzy.com/r4/QuestionnaireResponse" + query);
    var actual = controller.search(r);
    assertThat(actual.entry()).hasSize(1);
  }
  
  @ParameterizedTest
  @ValueSource(strings = {"", "?_id=123&authored=2000-01-01T00:00:00Z"})
  @SneakyThrows
  void invalidRequests(String query) {
    var r = _requestFromUri("http://fonzy.com/r4/QuestionnaireResponse" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(r));
  }
  
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
}
