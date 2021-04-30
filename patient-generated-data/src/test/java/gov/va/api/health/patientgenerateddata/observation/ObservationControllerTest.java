package gov.va.api.health.patientgenerateddata.observation;

import static gov.va.api.health.patientgenerateddata.MockRequests.requestFromUri;
import static org.assertj.core.api.Assertions.assertThat;
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
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Observation;
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

  private static Observation observation() {
    return Observation.builder().status(Observation.ObservationStatus.unknown).build();
  }

  private static Observation withAddedFields(
      Observation observation, String id, Instant lastUpdated) {
    observation.id(id);
    observation.meta(Meta.builder().lastUpdated(lastUpdated.toString()).build());
    return observation;
  }

  private ObservationController controller(ObservationRepository repo) {
    return new ObservationController(pageLinks, repo);
  }

  @Test
  @SneakyThrows
  void create() {
    Instant now = Instant.parse("2021-01-01T01:00:00.001Z");
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    ObservationRepository repo = mock(ObservationRepository.class);
    ObservationController controller = new ObservationController(pageLinks, repo);
    var observation = observation();
    var persisted = MAPPER.writeValueAsString(observation);
    var expectedObservation = withAddedFields(observation(), "123", now);
    assertThat(controller.create("123", now, observation))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Observation/" + 123))
                .body(expectedObservation));
    verify(repo, times(1)).save(ObservationEntity.builder().id("123").payload(persisted).build());
  }

  @Test
  @SneakyThrows
  void create_invalid() {
    var observation = observation().id("123");
    var repo = mock(ObservationRepository.class);
    var pageLinks = mock(LinkProperties.class);
    var controller = new ObservationController(pageLinks, repo);
    assertThrows(Exceptions.BadRequest.class, () -> controller.create(observation));
  }

  @Test
  void initDirectFieldAccess() {
    new ObservationController(mock(LinkProperties.class), mock(ObservationRepository.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  @SneakyThrows
  void read() {
    ObservationRepository repo = mock(ObservationRepository.class);
    String payload = MAPPER.writeValueAsString(Observation.builder().id("x").build());
    when(repo.findById("x"))
        .thenReturn(Optional.of(ObservationEntity.builder().id("x").payload(payload).build()));
    assertThat(new ObservationController(mock(LinkProperties.class), repo).read("x"))
        .isEqualTo(Observation.builder().id("x").build());
  }

  @Test
  void read_notFound() {
    ObservationRepository repo = mock(ObservationRepository.class);
    assertThrows(
        Exceptions.NotFound.class,
        () -> new ObservationController(mock(LinkProperties.class), repo).read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    Instant now = Instant.parse("2021-01-01T01:00:00.001Z");
    ObservationRepository repo = mock(ObservationRepository.class);
    Observation observation = Observation.builder().id("x").build();
    String payload = MAPPER.writeValueAsString(observation);
    when(repo.findById("x"))
        .thenReturn(Optional.of(ObservationEntity.builder().id("x").payload(payload).build()));
    Observation expectedObservation = withAddedFields(observation, "x", now);
    assertThat(new ObservationController(mock(LinkProperties.class), repo).update("x", observation))
        .isEqualTo(ResponseEntity.ok(expectedObservation));
    verify(repo, times(1)).save(ObservationEntity.builder().id("x").payload(payload).build());
  }

  @Test
  @SneakyThrows
  void update_not_existing() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    ObservationRepository repo = mock(ObservationRepository.class);
    Observation observation = Observation.builder().id("x").build();
    assertThrows(
        Exceptions.NotFound.class,
        () -> new ObservationController(pageLinks, repo).update("x", observation));
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = "?_id=1")
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
