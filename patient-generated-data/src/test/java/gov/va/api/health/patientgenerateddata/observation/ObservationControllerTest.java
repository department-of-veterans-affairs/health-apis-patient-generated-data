package gov.va.api.health.patientgenerateddata.observation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.r4.api.resources.Observation;

import java.net.URI;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;

public class ObservationControllerTest {
  @Test
  void initDirectFieldAccess() {
    new ObservationController(mock(LinkProperties.class), mock(ObservationRepository.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  @SneakyThrows
  void read() {
    ObservationRepository repo = mock(ObservationRepository.class);
    String payload =
        JacksonConfig.createMapper().writeValueAsString(Observation.builder().id("x").build());
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
    ObservationRepository repo = mock(ObservationRepository.class);
    Observation observation = Observation.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(observation);
    when(repo.findById("x"))
        .thenReturn(Optional.of(ObservationEntity.builder().id("x").payload(payload).build()));
    assertThat(new ObservationController(mock(LinkProperties.class), repo).update("x", observation))
        .isEqualTo(ResponseEntity.ok(observation));
    verify(repo, times(1)).save(ObservationEntity.builder().id("x").payload(payload).build());
  }

  @Test
  @SneakyThrows
  void update_new() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    ObservationRepository repo = mock(ObservationRepository.class);
    Observation observation = Observation.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(observation);
    assertThat(new ObservationController(pageLinks, repo).update("x", observation))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Observation/x"))
                .body(observation));
    verify(repo, times(1)).save(ObservationEntity.builder().id("x").payload(payload).build());
  }
}
