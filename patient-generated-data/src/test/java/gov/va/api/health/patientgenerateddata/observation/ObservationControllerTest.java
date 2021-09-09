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

  LinkProperties pageLinks =
      LinkProperties.builder()
          .defaultPageSize(500)
          .maxPageSize(20)
          .baseUrl("http://foo.com")
          .r4BasePath("r4")
          .build();

  Sourcerer sourcerer = new Sourcerer("{}", "sat");

  ObservationRepository repo = mock(ObservationRepository.class);

  ObservationController _controller() {
    return new ObservationController(pageLinks, repo, sourcerer);
  }

  @Test
  @SneakyThrows
  void create() {
    Instant time = Instant.parse("2021-01-01T01:00:00.001Z");
    assertThat(_controller().create(observation(), "Bearer sat", null, time))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Observation/x"))
                .body(
                    observationWithLastUpdatedAndSource(
                        time, "https://api.va.gov/services/pgd/static-access")));
    verify(repo, times(1))
        .save(
            ObservationEntity.builder()
                .id("x")
                .payload(MAPPER.writeValueAsString(observation()))
                .build());
  }

  @Test
  void create_invalidExistingId() {
    assertThrows(
        Exceptions.BadRequest.class, () -> _controller().create(observation(), "Bearer sat", null));
  }

  @Test
  @SneakyThrows
  void getAllIds() {
    when(repo.findAll())
        .thenReturn(
            List.of(
                ObservationEntity.builder()
                    .id("x1")
                    .payload(MAPPER.writeValueAsString(observation("x1")))
                    .build(),
                ObservationEntity.builder()
                    .id("x2")
                    .payload(MAPPER.writeValueAsString(observation("x2")))
                    .build(),
                ObservationEntity.builder()
                    .id("x3")
                    .payload(MAPPER.writeValueAsString(observation("x3")))
                    .build()));
    assertThat(_controller().getAllIds()).isEqualTo(List.of("x1", "x2", "x3"));
  }

  @Test
  void initDirectFieldAccess() {
    _controller().initDirectFieldAccess(mock(DataBinder.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "?_id=123&_lastUpdated=gt2020"})
  void invalidRequests(String query) {
    assertThatExceptionOfType(InvalidRequest.class)
        .isThrownBy(
            () -> _controller().search(requestFromUri("http://fonzy.com/r4/Observation" + query)));
  }

  @Test
  @SneakyThrows
  void read() {
    when(repo.findById("x"))
        .thenReturn(
            Optional.of(
                ObservationEntity.builder()
                    .id("x")
                    .payload(MAPPER.writeValueAsString(observation()))
                    .build()));
    assertThat(_controller().read("x")).isEqualTo(observation());
  }

  @Test
  void read_notFound() {
    assertThrows(Exceptions.NotFound.class, () -> _controller().read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    Instant oldTime = Instant.parse("2021-01-01T01:00:00.001Z");
    Instant newTime = Instant.parse("2022-02-02T02:00:00.002Z");
    when(repo.findById("x"))
        .thenReturn(
            Optional.of(
                ObservationEntity.builder()
                    .id("x")
                    .payload(
                        MAPPER.writeValueAsString(
                            observationWithLastUpdatedAndSource(
                                oldTime, "https://api.va.gov/services/pgd/static-access")))
                    .build()));
    assertThat(
            _controller()
                .update(
                    observationWithLastUpdatedAndSource(
                        newTime, "https://api.va.gov/services/pgd/static-access"),
                    "Bearer sat",
                    null,
                    newTime))
        .isEqualTo(
            ResponseEntity.ok(
                observationWithLastUpdatedAndSource(
                    newTime, "https://api.va.gov/services/pgd/static-access")));
    verify(repo, times(1))
        .save(
            ObservationEntity.builder()
                .id("x")
                .payload(
                    MAPPER.writeValueAsString(
                        observationWithLastUpdatedAndSource(
                            newTime, "https://api.va.gov/services/pgd/static-access")))
                .build());
  }

  @Test
  void update_not_existing() {
    assertThrows(
        Exceptions.NotFound.class,
        () -> _controller().update("x", observation(), "Bearer sat", null));
  }

  @Test
  @SneakyThrows
  void update_payloadSource() {
    Instant oldTime = Instant.parse("2021-01-01T01:00:00.001Z");
    Instant newTime = Instant.parse("2022-02-02T02:00:00.002Z");

    when(repo.findById("x"))
        .thenReturn(
            Optional.of(
                ObservationEntity.builder()
                    .id("x")
                    .payload(
                        MAPPER.writeValueAsString(
                            observationWithLastUpdatedAndSource(
                                oldTime, "https://api.va.gov/services/pgd/static-access")))
                    .build()));
    assertThat(
            _controller()
                .update(
                    observationWithLastUpdatedAndSource(
                        newTime, "https://api.va.gov/services/pgd/zombie-bob-nelson"),
                    "Bearer sat",
                    null,
                    newTime))
        .isEqualTo(
            ResponseEntity.ok(
                observationWithLastUpdatedAndSource(
                    newTime, "https://api.va.gov/services/pgd/static-access")));
    verify(repo, times(1))
        .save(
            ObservationEntity.builder()
                .id("x")
                .payload(
                    MAPPER.writeValueAsString(
                        observationWithLastUpdatedAndSource(
                            newTime, "https://api.va.gov/services/pgd/static-access")))
                .build());
  }

  @ParameterizedTest
  @ValueSource(strings = {"?_id=1", "?_lastUpdated=gt2020"})
  void validSearch(String query) {
    when(repo.findAll(
            ArgumentMatchers.<Specification<ObservationEntity>>any(), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl<ObservationEntity>(
                    List.of(ObservationEntity.builder().build().id("1").payload("{ \"id\": 1}")),
                    i.getArgument(1, Pageable.class),
                    1));
    assertThat(
            _controller().search(requestFromUri("http://fonzy.com/r4/Observation" + query)).entry())
        .hasSize(1);
  }
}
