package gov.va.api.health.patientgenerateddata.observation;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static gov.va.api.health.patientgenerateddata.Controllers.generateRandomId;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.r4.api.resources.Observation;
import java.net.URI;
import java.util.Optional;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(
    value = "/r4/Observation",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ObservationController {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private final LinkProperties linkProperties;

  private final ObservationRepository repository;

  @PostMapping
  ResponseEntity<Observation> create(@Valid @RequestBody Observation observation) {
    return create(generateRandomId(), observation);
  }

  ResponseEntity<Observation> create(String id, Observation observation) {
    checkRequestState(isEmpty(observation.id()), "ID must be empty, found %s", observation.id());
    observation.id(id);
    ObservationEntity entity = toEntity(observation);
    repository.save(entity);
    return ResponseEntity.created(URI.create(linkProperties.r4Url() + "/Observation/" + id))
        .body(observation);
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @SneakyThrows
  ObservationEntity populate(Observation observation, ObservationEntity entity) {
    return populate(observation, entity, MAPPER.writeValueAsString(observation));
  }

  ObservationEntity populate(
      @NonNull Observation observation, @NonNull ObservationEntity entity, String payload) {
    checkState(
        entity.id().equals(observation.id()),
        "IDs don't match, %s != %s",
        entity.id(),
        observation.id());
    entity.payload(payload);
    return entity;
  }

  @GetMapping(value = "/{id}")
  Observation read(@PathVariable("id") String id) {
    Optional<ObservationEntity> maybeEntity = repository.findById(id);
    ObservationEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    return entity.deserializePayload();
  }

  ObservationEntity toEntity(Observation observation) {
    checkState(observation.id() != null, "ID is required");
    return populate(observation, ObservationEntity.builder().id(observation.id()).build());
  }

  @SneakyThrows
  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Observation> update(
      @PathVariable("id") String id, @Valid @RequestBody Observation observation) {
    checkState(id.equals(observation.id()), "%s != %s", id, observation.id());
    Optional<ObservationEntity> maybeEntity = repository.findById(id);
    ObservationEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    entity = populate(observation, entity);
    repository.save(entity);
    return ResponseEntity.ok(observation);
  }
}
