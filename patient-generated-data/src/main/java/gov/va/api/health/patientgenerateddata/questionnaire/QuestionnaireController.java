package gov.va.api.health.patientgenerateddata.questionnaire;

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
import gov.va.api.health.patientgenerateddata.CompositeMapping;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.JacksonMapperConfig;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.patientgenerateddata.VulcanizedBundler;
import gov.va.api.health.r4.api.resources.Questionnaire;
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

/** R4 Questionnaire controller. */
@Validated
@RestController
@RequestMapping(
    value = "/r4/Questionnaire",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class QuestionnaireController {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private final LinkProperties linkProperties;

  private final QuestionnaireRepository repository;

  /** Populates an entity with Resource data. */
  @SneakyThrows
  private static QuestionnaireEntity populate(
      Questionnaire questionnaire, QuestionnaireEntity entity) {
    return populate(questionnaire, entity, MAPPER.writeValueAsString(questionnaire));
  }

  private static QuestionnaireEntity populate(
      @NonNull Questionnaire questionnaire, @NonNull QuestionnaireEntity entity, String payload) {
    checkState(
        entity.id().equals(questionnaire.id()),
        "IDs don't match, %s != %s",
        entity.id(),
        questionnaire.id());
    entity.payload(payload);
    entity.contextTypeValue(CompositeMapping.useContextValueJoin(questionnaire));

    Optional<Instant> maybeLastUpdated = lastUpdatedFromMeta(questionnaire.meta());
    if (maybeLastUpdated.isPresent()) {
      entity.lastUpdated(maybeLastUpdated.get());
    }
    return entity;
  }

  public static QuestionnaireEntity toEntity(Questionnaire questionnaire) {
    checkState(questionnaire.id() != null, "ID is required");
    return populate(questionnaire, QuestionnaireEntity.builder().id(questionnaire.id()).build());
  }

  private VulcanConfiguration<QuestionnaireEntity> configuration() {
    return VulcanConfiguration.forEntity(QuestionnaireEntity.class)
        .paging(
            linkProperties.pagingConfiguration("Questionnaire", QuestionnaireEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(QuestionnaireEntity.class)
                .value("_id", "id")
                .dateAsInstant("_lastUpdated", "lastUpdated")
                .add(
                    CompositeMapping.<QuestionnaireEntity>builder()
                        .parameterName("context-type-value")
                        .fieldName("contextTypeValue")
                        .build())
                .get())
        .defaultQuery(returnNothing())
        .rules(
            List.of(
                atLeastOneParameterOf("_id", "_lastUpdated", "context-type-value"),
                ifParameter("_id").thenForbidParameters("_lastUpdated", "context-type-value")))
        .build();
  }

  @PostMapping
  ResponseEntity<Questionnaire> create(@Valid @RequestBody Questionnaire questionnaire) {
    return create(generateRandomId(), nowMillis(), questionnaire);
  }

  @SneakyThrows
  ResponseEntity<Questionnaire> create(String id, Instant now, Questionnaire questionnaire) {
    checkRequestState(
        isEmpty(questionnaire.id()), "ID must be empty, found %s", questionnaire.id());
    questionnaire.id(id);
    questionnaire.meta(metaWithLastUpdated(questionnaire.meta(), now));
    QuestionnaireEntity entity = toEntity(questionnaire);
    repository.save(entity);
    return ResponseEntity.created(URI.create(linkProperties.r4Url() + "/Questionnaire/" + id))
        .body(questionnaire);
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @GetMapping(value = "/{id}")
  Questionnaire read(@PathVariable("id") String id) {
    Optional<QuestionnaireEntity> maybeEntity = repository.findById(id);
    QuestionnaireEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    return entity.deserializePayload();
  }

  @GetMapping
  Questionnaire.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<QuestionnaireEntity, Questionnaire, Questionnaire.Entry, Questionnaire.Bundle>
      toBundle() {
    return VulcanizedBundler.forBundling(
            QuestionnaireEntity.class,
            VulcanizedBundler.Bundling.newBundle(Questionnaire.Bundle::new)
                .newEntry(Questionnaire.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .toResource(QuestionnaireEntity::deserializePayload)
        .build();
  }

  @SneakyThrows
  ResponseEntity<Questionnaire> update(String id, Instant now, Questionnaire questionnaire) {
    checkState(id.equals(questionnaire.id()), "%s != %s", id, questionnaire.id());
    questionnaire.meta(metaWithLastUpdated(questionnaire.meta(), now));
    String payload = MAPPER.writeValueAsString(questionnaire);
    Optional<QuestionnaireEntity> maybeEntity = repository.findById(questionnaire.id());
    QuestionnaireEntity entity =
        maybeEntity.orElseThrow(() -> new Exceptions.NotFound(questionnaire.id()));
    entity = populate(questionnaire, entity, payload);
    repository.save(entity);
    return ResponseEntity.ok(questionnaire);
  }

  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Questionnaire> update(
      @PathVariable("id") String id, @Valid @RequestBody Questionnaire questionnaire) {
    return update(id, nowMillis(), questionnaire);
  }
}
