package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static gov.va.api.health.patientgenerateddata.MockRequests.requestFromUri;
import static gov.va.api.health.patientgenerateddata.questionnaireresponse.Samples.questionnaireResponse;
import static gov.va.api.health.patientgenerateddata.questionnaireresponse.Samples.questionnaireResponseWithLastUpdated;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.patientgenerateddata.ClientIdMajig;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.JacksonMapperConfig;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
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

public class QuestionnaireResponseControllerTest {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  static LinkProperties pageLinks =
      LinkProperties.builder()
          .defaultPageSize(500)
          .maxPageSize(20)
          .baseUrl("http://foo.com")
          .r4BasePath("r4")
          .build();

  private static QuestionnaireResponseController controller() {
    return controller(mock(QuestionnaireResponseRepository.class));
  }

  private static QuestionnaireResponseController controller(QuestionnaireResponseRepository repo) {
    return new QuestionnaireResponseController(pageLinks, repo, new ClientIdMajig("{}"));
  }

  @Test
  @SneakyThrows
  void create() {
    Instant now = Instant.parse("2021-01-01T01:00:00.001Z");
    var expected = questionnaireResponseWithLastUpdated(now);
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    assertThat(controller(repo).create(questionnaireResponse().id("x"), now))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/QuestionnaireResponse/x"))
                .body(expected));
    verify(repo, times(1))
        .save(
            QuestionnaireResponseEntity.builder()
                .id("x")
                .payload(MAPPER.writeValueAsString(expected))
                .build());
  }

  @Test
  void create_invalid() {
    var questionnaireResponse = questionnaireResponse().id("123");
    var repo = mock(QuestionnaireResponseRepository.class);
    var pageLinks = mock(LinkProperties.class);
    var controller = new QuestionnaireResponseController(pageLinks, repo, new ClientIdMajig("{}"));
    assertThrows(Exceptions.BadRequest.class, () -> controller.create(questionnaireResponse));
  }

  @Test
  void initDirectFieldAccess() {
    new QuestionnaireResponseController(
            mock(LinkProperties.class),
            mock(QuestionnaireResponseRepository.class),
            new ClientIdMajig("{}"))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"", "?_id=123&authored=2000-01-01T00:00:00Z", "?_id=123&_lastUpdated=gt2020"})
  void invalidRequests(String query) {
    var r = requestFromUri("http://fonzy.com/r4/QuestionnaireResponse" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(r));
  }

  @Test
  @SneakyThrows
  void read() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    String payload = MAPPER.writeValueAsString(questionnaireResponse());
    when(repo.findById("x"))
        .thenReturn(
            Optional.of(QuestionnaireResponseEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new QuestionnaireResponseController(pageLinks, repo, new ClientIdMajig("{}")).read("x"))
        .isEqualTo(questionnaireResponse());
  }

  @Test
  void read_notFound() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    assertThrows(
        Exceptions.NotFound.class,
        () ->
            new QuestionnaireResponseController(pageLinks, repo, new ClientIdMajig("{}"))
                .read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    Instant now = Instant.parse("2021-01-01T01:00:00.001Z");
    QuestionnaireResponse questionnaireResponse = questionnaireResponse();
    String payload = MAPPER.writeValueAsString(questionnaireResponse);
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    when(repo.findById("x"))
        .thenReturn(
            Optional.of(QuestionnaireResponseEntity.builder().id("x").payload(payload).build()));
    QuestionnaireResponse expected = questionnaireResponseWithLastUpdated(now);
    assertThat(
            new QuestionnaireResponseController(pageLinks, repo, new ClientIdMajig("{}"))
                .update(questionnaireResponse, now))
        .isEqualTo(ResponseEntity.ok(expected));
    verify(repo, times(1))
        .save(QuestionnaireResponseEntity.builder().id("x").payload(payload).build());
  }

  @Test
  void update_not_existing() {
    QuestionnaireResponseRepository repo = mock(QuestionnaireResponseRepository.class);
    QuestionnaireResponse questionnaireResponse = questionnaireResponse();
    assertThrows(
        Exceptions.NotFound.class,
        () ->
            new QuestionnaireResponseController(pageLinks, repo, new ClientIdMajig("{}"))
                .update("x", questionnaireResponse));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=1",
        "?_lastUpdated=gt2020",
        "?author=1011537977V693883",
        "?questionnaire=37953b72-961b-41ee-bd05-86c62bacc46b"
      })
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
