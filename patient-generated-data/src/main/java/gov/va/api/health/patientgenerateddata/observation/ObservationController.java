package gov.va.api.health.patientgenerateddata.observation;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static gov.va.api.health.patientgenerateddata.Controllers.generateRandomId;
import static gov.va.api.health.patientgenerateddata.Controllers.lastUpdatedFromMeta;
import static gov.va.api.health.patientgenerateddata.Controllers.metaWithLastUpdated;
import static gov.va.api.health.patientgenerateddata.Controllers.nowMillis;
import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.JacksonMapperConfig;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.patientgenerateddata.VulcanizedBundler;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
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

/** R4 Observation Controller. */
@Validated
@RestController
@RequestMapping(
    value = "/r4/Observation",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ObservationController {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private final LinkProperties linkProperties;

  private final ObservationRepository repository;

  @SneakyThrows
  private static ObservationEntity populate(Observation observation, ObservationEntity entity) {
    return populate(observation, entity, MAPPER.writeValueAsString(observation));
  }

  private static ObservationEntity populate(
      @NonNull Observation observation, @NonNull ObservationEntity entity, String payload) {
    checkState(
        entity.id().equals(observation.id()),
        "IDs don't match, %s != %s",
        entity.id(),
        observation.id());
    entity.payload(payload);

    Optional<Instant> maybeLastUpdated = lastUpdatedFromMeta(observation.meta());
    if (maybeLastUpdated.isPresent()) {
      // entity.lastUpdated(maybeLastUpdated.get());
    }

    return entity;
  }

  /** Transforms a Resource to an Entity. */
  public static ObservationEntity toEntity(Observation observation) {
    checkState(observation.id() != null, "ID is required");
    return populate(observation, ObservationEntity.builder().id(observation.id()).build());
  }

  private VulcanConfiguration<ObservationEntity> configuration() {
    return VulcanConfiguration.forEntity(ObservationEntity.class)
        .paging(linkProperties.pagingConfiguration("Observation", ObservationEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(ObservationEntity.class)
                .value("_id", "id")
                .dateAsInstant("_lastUpdated", "lastUpdated")
                .get())
        .defaultQuery(returnNothing())
        .rules(
            List.of(
                atLeastOneParameterOf("_id", "_lastUpdated"),
                ifParameter("_id").thenForbidParameters("_lastUpdated")))
        .build();
  }

  @PostMapping
  ResponseEntity<Observation> create(@Valid @RequestBody Observation observation) {
    return create(generateRandomId(), nowMillis(), observation);
  }

  ResponseEntity<Observation> create(String id, Instant now, Observation observation) {
    checkRequestState(isEmpty(observation.id()), "ID must be empty, found %s", observation.id());
    observation.id(id);
    observation.meta(metaWithLastUpdated(observation.meta(), now));
    ObservationEntity entity = toEntity(observation);
    repository.save(entity);
    return ResponseEntity.created(URI.create(linkProperties.r4Url() + "/Observation/" + id))
        .body(observation);
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @GetMapping(value = "/{id}")
  Observation read(@PathVariable("id") String id) {
    Optional<ObservationEntity> maybeEntity = repository.findById(id);
    ObservationEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    return entity.deserializePayload();
  }

  @GetMapping
  Observation.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<ObservationEntity, Observation, Observation.Entry, Observation.Bundle>
      toBundle() {
    return VulcanizedBundler.forBundling(
            ObservationEntity.class,
            VulcanizedBundler.Bundling.newBundle(Observation.Bundle::new)
                .newEntry(Observation.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .toResource(ObservationEntity::deserializePayload)
        .build();
  }

  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Observation> update(
      @PathVariable("id") String id, @Valid @RequestBody Observation observation) {
    return update(id, nowMillis(), observation);
  }

  ResponseEntity<Observation> update(String id, Instant now, Observation observation) {
    checkState(id.equals(observation.id()), "%s != %s", id, observation.id());
    observation.meta(metaWithLastUpdated(observation.meta(), now));
    Optional<ObservationEntity> maybeEntity = repository.findById(observation.id());
    ObservationEntity entity =
        maybeEntity.orElseThrow(() -> new Exceptions.NotFound(observation.id()));
    entity = populate(observation, entity);
    repository.save(entity);
    return ResponseEntity.ok(observation);
  }
}
