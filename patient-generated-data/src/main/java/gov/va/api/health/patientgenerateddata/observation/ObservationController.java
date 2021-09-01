package gov.va.api.health.patientgenerateddata.observation;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static gov.va.api.health.patientgenerateddata.Controllers.generateRandomId;
import static gov.va.api.health.patientgenerateddata.Controllers.lastUpdatedFromMeta;
import static gov.va.api.health.patientgenerateddata.Controllers.metaWithLastUpdated;
import static gov.va.api.health.patientgenerateddata.Controllers.metaWithSource;
import static gov.va.api.health.patientgenerateddata.Controllers.nowMillis;
import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.JacksonMapperConfig;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.patientgenerateddata.Sourcerer;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  private final Sourcerer sourcerer;

  @SneakyThrows
  private static void populateEntity(
      @NonNull ObservationEntity entity, @NonNull Observation observation) {
    checkState(
        entity.id().equals(observation.id()),
        "Entity ID (%s) and payload ID (%s) do not match",
        entity.id(),
        observation.id());
    entity.payload(MAPPER.writeValueAsString(observation));
    entity.lastUpdated(lastUpdatedFromMeta(observation.meta()).orElse(null));
  }

  private static ObservationEntity toEntity(Observation observation) {
    checkState(!isBlank(observation.id()), "ID is required");
    ObservationEntity entity = ObservationEntity.builder().id(observation.id()).build();
    populateEntity(entity, observation);
    return entity;
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
  @Loggable(arguments = false)
  ResponseEntity<Observation> create(
      @Valid @RequestBody Observation observation,
      @RequestHeader(name = "Authorization", required = true) String authorization) {
    checkRequestState(isEmpty(observation.id()), "ID must be empty, found %s", observation.id());
    observation.id(generateRandomId());
    return create(observation, authorization, nowMillis());
  }

  /** Create resource. */
  public ResponseEntity<Observation> create(
      Observation observation, String authorization, Instant now) {
    observation.meta(metaWithSource(observation.meta(), sourcerer.source(authorization)));
    observation.meta(metaWithLastUpdated(observation.meta(), now));
    repository.save(toEntity(observation));
    return ResponseEntity.created(
            URI.create(linkProperties.r4Url() + "/Observation/" + observation.id()))
        .body(observation);
  }

  public Optional<Observation> findById(String id) {
    return repository.findById(id).map(e -> e.deserializePayload());
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @GetMapping(value = "/{id}")
  Observation read(@PathVariable("id") String id) {
    return findById(id).orElseThrow(() -> new Exceptions.NotFound(id));
  }

  @GetMapping
  Observation.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  private VulcanizedBundler<ObservationEntity, Observation, Observation.Entry, Observation.Bundle>
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
      @PathVariable("id") String pathId,
      @Valid @RequestBody Observation observation,
      @RequestHeader(name = "Authorization", required = true) String authorization) {
    checkRequestState(
        pathId.equals(observation.id()),
        "Path ID (%s) and request body ID (%s) do not match",
        pathId,
        observation.id());
    return update(observation, authorization, nowMillis());
  }

  ResponseEntity<Observation> update(Observation observation, String authorization, Instant now) {
    observation.meta(metaWithSource(observation.meta(), sourcerer.source(authorization)));
    observation.meta(metaWithLastUpdated(observation.meta(), now));
    ObservationEntity entity =
        repository
            .findById(observation.id())
            .orElseThrow(() -> new Exceptions.NotFound(observation.id()));
    populateEntity(entity, observation);
    repository.save(entity);
    return ResponseEntity.ok(observation);
  }
}
