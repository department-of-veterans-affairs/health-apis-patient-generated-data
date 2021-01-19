package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static gov.va.api.health.patientgenerateddata.Controllers.generateRandomId;
import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.patientgenerateddata.ParseUtils;
import gov.va.api.health.patientgenerateddata.ReferenceUtils;
import gov.va.api.health.patientgenerateddata.VulcanizedBundler;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.net.URI;
import java.time.Instant;
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

@Validated
@RestController
@RequestMapping(
    value = "/r4/QuestionnaireResponse",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class QuestionnaireResponseController {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private final LinkProperties linkProperties;

  private final QuestionnaireResponseRepository repository;

  /** Populates an entity with Resource data. */
  @SneakyThrows
  public static QuestionnaireResponseEntity populate(
      QuestionnaireResponse questionnaireResponse, QuestionnaireResponseEntity entity) {
    return populate(
        questionnaireResponse, entity, MAPPER.writeValueAsString(questionnaireResponse));
  }

  /** Populates an entity with Resource data. */
  public static QuestionnaireResponseEntity populate(
      @NonNull QuestionnaireResponse questionnaireResponse,
      @NonNull QuestionnaireResponseEntity entity,
      String payload) {
    checkState(
        entity.id().equals(questionnaireResponse.id()),
        "IDs don't match, %s != %s",
        entity.id(),
        questionnaireResponse.id());
    String authorId = ReferenceUtils.resourceId(questionnaireResponse.author());
    Instant authored = ParseUtils.parseDateTime(questionnaireResponse.authored());
    String subject = ReferenceUtils.resourceId(questionnaireResponse.subject());
    entity.payload(payload);
    entity.author(authorId);
    entity.authored(authored);
    entity.subject(subject);
    return entity;
  }

  /** Transforms a Resource to an Entity. */
  public static QuestionnaireResponseEntity toEntity(QuestionnaireResponse questionnaireResponse) {
    checkState(questionnaireResponse.id() != null, "ID is required");
    return populate(
        questionnaireResponse,
        QuestionnaireResponseEntity.builder().id(questionnaireResponse.id()).build());
  }

  private VulcanConfiguration<QuestionnaireResponseEntity> configuration() {
    return VulcanConfiguration.forEntity(QuestionnaireResponseEntity.class)
        .paging(
            linkProperties.pagingConfiguration(
                "QuestionnaireResponse", QuestionnaireResponseEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(QuestionnaireResponseEntity.class)
                .value("_id", "id")
                .value("author", "author")
                .dateAsInstant("authored", "authored")
                .value("subject", "subject")
                .get())
        .defaultQuery(returnNothing())
        .rule(atLeastOneParameterOf("_id", "author", "authored", "subject"))
        .rule(ifParameter("_id").thenForbidParameters("author", "authored", "subject"))
        .build();
  }

  @PostMapping
  ResponseEntity<QuestionnaireResponse> create(
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse) {
    return create(generateRandomId(), questionnaireResponse);
  }

  ResponseEntity<QuestionnaireResponse> create(
      String id, QuestionnaireResponse questionnaireResponse) {
    checkRequestState(
        isEmpty(questionnaireResponse.id()),
        "ID must be empty, found %s",
        questionnaireResponse.id());
    questionnaireResponse.id(id);
    QuestionnaireResponseEntity entity = toEntity(questionnaireResponse);
    repository.save(entity);
    return ResponseEntity.created(
            URI.create(linkProperties.r4Url() + "/QuestionnaireResponse/" + id))
        .body(questionnaireResponse);
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @GetMapping(value = "/{id}")
  QuestionnaireResponse read(@PathVariable("id") String id) {
    Optional<QuestionnaireResponseEntity> maybeEntity = repository.findById(id);
    QuestionnaireResponseEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    return entity.deserializePayload();
  }

  /** QuestionnaireResponse Search. */
  @GetMapping
  QuestionnaireResponse.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          QuestionnaireResponseEntity,
          QuestionnaireResponse,
          QuestionnaireResponse.Entry,
          QuestionnaireResponse.Bundle>
      toBundle() {
    return VulcanizedBundler.forBundling(
            QuestionnaireResponseEntity.class,
            VulcanizedBundler.Bundling.newBundle(QuestionnaireResponse.Bundle::new)
                .newEntry(QuestionnaireResponse.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .toResource(QuestionnaireResponseEntity::deserializePayload)
        .build();
  }

  @SneakyThrows
  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<QuestionnaireResponse> update(
      @PathVariable("id") String id,
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse) {
    checkState(id.equals(questionnaireResponse.id()), "%s != %s", id, questionnaireResponse.id());
    Optional<QuestionnaireResponseEntity> maybeEntity = repository.findById(id);
    QuestionnaireResponseEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    entity = populate(questionnaireResponse, entity);
    repository.save(entity);
    return ResponseEntity.ok(questionnaireResponse);
  }
}
