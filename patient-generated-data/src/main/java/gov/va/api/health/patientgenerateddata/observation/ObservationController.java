package gov.va.api.health.patientgenerateddata.observation;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.SerializationUtils.deserializedPayload;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.r4.api.resources.Observation;
import java.net.URI;
import java.util.Optional;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
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
  private final LinkProperties linkProperties;

  private final ObservationRepository repository;

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @GetMapping(value = "/{id}")
  Observation read(@PathVariable("id") String id) {
    Optional<ObservationEntity> maybeEntity = repository.findById(id);
    ObservationEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    return deserializedPayload(id, entity.payload(), Observation.class);
  }

  @SneakyThrows
  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Observation> update(
      @PathVariable("id") String id, @Valid @RequestBody Observation observation) {
    String payload = JacksonConfig.createMapper().writeValueAsString(observation);
    checkState(id.equals(observation.id()), "%s != %s", id, observation.id());
    Optional<ObservationEntity> maybeEntity = repository.findById(id);
    if (maybeEntity.isPresent()) {
      ObservationEntity entity = maybeEntity.get();
      entity.payload(payload);
      repository.save(entity);
      return ResponseEntity.ok(observation);
    }
    repository.save(ObservationEntity.builder().id(id).payload(payload).build());
    return ResponseEntity.created(URI.create(linkProperties.r4Url() + "/Observation/" + id))
        .body(observation);
  }
}
