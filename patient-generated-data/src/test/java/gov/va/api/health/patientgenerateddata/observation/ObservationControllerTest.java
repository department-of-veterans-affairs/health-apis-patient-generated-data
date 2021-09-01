package gov.va.api.health.patientgenerateddata.observation;

import static gov.va.api.health.patientgenerateddata.MockRequests.requestFromUri;
import static gov.va.api.health.patientgenerateddata.observation.Samples.observation;
import static gov.va.api.health.patientgenerateddata.observation.Samples.observationWithLastUpdatedAndSource;
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
import gov.va.api.health.r4.api.resources.Observation;
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

public class ObservationControllerTest {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  static LinkProperties pageLinks =
      LinkProperties.builder()
          .defaultPageSize(500)
          .maxPageSize(20)
          .baseUrl("http://foo.com")
          .r4BasePath("r4")
          .build();

  private static ObservationController controller(ObservationRepository repo) {
    return new ObservationController(pageLinks, repo, new Sourcerer("{}", "sat"));
  }

  private static ObservationController controller() {
    return controller(mock(ObservationRepository.class));
  }

  @Test
  @SneakyThrows
  void create() {
    Instant time = Instant.parse("2021-01-01T01:00:00.001Z");
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    ObservationRepository repo = mock(ObservationRepository.class);
    ObservationController controller =
        new ObservationController(pageLinks, repo, new Sourcerer("{}", "sat"));
    var observation = observation();
    var persisted = MAPPER.writeValueAsString(observation);
    var expectedObservation =
        observationWithLastUpdatedAndSource(time, "https://api.va.gov/services/pgd/static-access");
    assertThat(controller.create(observation, "Bearer sat", time))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Observation/x"))
                .body(expectedObservation));
    verify(repo, times(1)).save(ObservationEntity.builder().id("x").payload(persisted).build());
  }

  @Test
  void create_invalid() {
    var observation = observation().id("123");
    var repo = mock(ObservationRepository.class);
    var pageLinks = mock(LinkProperties.class);
    var controller = new ObservationController(pageLinks, repo, new Sourcerer("{}", "sat"));
    assertThrows(Exceptions.BadRequest.class, () -> controller.create(observation, ""));
  }

  @Test
  void initDirectFieldAccess() {
    new ObservationController(
            mock(LinkProperties.class),
            mock(ObservationRepository.class),
            new Sourcerer("{}", "sat"))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "?_id=123&_lastUpdated=gt2020"})
  void invalidRequests(String query) {
    var r = requestFromUri("http://fonzy.com/r4/Observation" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(r));
  }

  @Test
  @SneakyThrows
  void read() {
    ObservationRepository repo = mock(ObservationRepository.class);
    String payload = MAPPER.writeValueAsString(observation());
    when(repo.findById("x"))
        .thenReturn(Optional.of(ObservationEntity.builder().id("x").payload(payload).build()));
    assertThat(
            new ObservationController(mock(LinkProperties.class), repo, new Sourcerer("{}", "sat"))
                .read("x"))
        .isEqualTo(observation());
  }

  @Test
  void read_notFound() {
    ObservationRepository repo = mock(ObservationRepository.class);
    assertThrows(
        Exceptions.NotFound.class,
        () ->
            new ObservationController(mock(LinkProperties.class), repo, new Sourcerer("{}", "sat"))
                .read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    Instant time = Instant.parse("2021-01-01T01:00:00.001Z");
    ObservationRepository repo = mock(ObservationRepository.class);
    String payload = MAPPER.writeValueAsString(observation());
    when(repo.findById("x"))
        .thenReturn(Optional.of(ObservationEntity.builder().id("x").payload(payload).build()));
    Observation hasLastUpdated =
        observationWithLastUpdatedAndSource(time, "https://api.va.gov/services/pgd/static-access");
    assertThat(
            new ObservationController(mock(LinkProperties.class), repo, new Sourcerer("{}", "sat"))
                .update("x", hasLastUpdated, "Bearer sat"))
        .isEqualTo(ResponseEntity.ok(hasLastUpdated));
    verify(repo, times(1)).save(ObservationEntity.builder().id("x").payload(payload).build());
  }

  @Test
  void update_not_existing() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    ObservationRepository repo = mock(ObservationRepository.class);
    Observation observation = observation();
    assertThrows(
        Exceptions.NotFound.class,
        () ->
            new ObservationController(pageLinks, repo, new Sourcerer("{}", "sat"))
                .update("x", observation, "Bearer sat"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"?_id=1", "?_lastUpdated=gt2020"})
  void validSearch(String query) {
    ObservationRepository repo = mock(ObservationRepository.class);
    ObservationController controller = controller(repo);
    var anySpec = ArgumentMatchers.<Specification<ObservationEntity>>any();
    when(repo.findAll(anySpec, any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl<ObservationEntity>(
                    List.of(ObservationEntity.builder().build().id("1").payload("{ \"id\": 1}")),
                    i.getArgument(1, Pageable.class),
                    1));
    var r = requestFromUri("http://fonzy.com/r4/Observation" + query);
    var actual = controller.search(r);
    assertThat(actual.entry()).hasSize(1);
  }
}
