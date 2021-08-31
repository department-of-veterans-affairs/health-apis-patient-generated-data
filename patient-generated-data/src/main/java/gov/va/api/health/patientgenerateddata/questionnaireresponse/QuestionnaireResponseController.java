package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static gov.va.api.health.patientgenerateddata.Controllers.generateRandomId;
import static gov.va.api.health.patientgenerateddata.Controllers.lastUpdatedFromMeta;
import static gov.va.api.health.patientgenerateddata.Controllers.metaWithLastUpdated;
import static gov.va.api.health.patientgenerateddata.Controllers.nowMillis;
import static gov.va.api.health.patientgenerateddata.Controllers.parseDateTime;
import static gov.va.api.health.patientgenerateddata.Controllers.resourceId;
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
import gov.va.api.health.patientgenerateddata.TokenListMapping;
import gov.va.api.health.patientgenerateddata.VulcanizedBundler;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.net.URI;
import java.time.Instant;
import java.util.List;
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
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private final LinkProperties linkProperties;

  private final QuestionnaireResponseRepository repository;

  private final Sourcerer cim;

  @SneakyThrows
  private static void populateEntity(
      @NonNull QuestionnaireResponseEntity entity,
      @NonNull QuestionnaireResponse questionnaireResponse) {
    checkState(
        entity.id().equals(questionnaireResponse.id()),
        "Entity ID (%s) and payload ID (%s) do not match",
        entity.id(),
        questionnaireResponse.id());
    entity.payload(MAPPER.writeValueAsString(questionnaireResponse));
    entity.author(resourceId(questionnaireResponse.author()));
    entity.authored(parseDateTime(questionnaireResponse.authored()));
    entity.questionnaire(resourceId(questionnaireResponse.questionnaire()));
    entity.subject(resourceId(questionnaireResponse.subject()));
    entity.metaTag(TokenListMapping.metadataTagJoin(questionnaireResponse));
    entity.source(resourceId(questionnaireResponse.source()));
    entity.lastUpdated(lastUpdatedFromMeta(questionnaireResponse.meta()).orElse(null));
  }

  private static QuestionnaireResponseEntity toEntity(QuestionnaireResponse questionnaireResponse) {
    checkState(!isBlank(questionnaireResponse.id()), "ID is required");
    QuestionnaireResponseEntity entity =
        QuestionnaireResponseEntity.builder().id(questionnaireResponse.id()).build();
    populateEntity(entity, questionnaireResponse);
    return entity;
  }

  private VulcanConfiguration<QuestionnaireResponseEntity> configuration() {
    return VulcanConfiguration.forEntity(QuestionnaireResponseEntity.class)
        .paging(
            linkProperties.pagingConfiguration(
                "QuestionnaireResponse", QuestionnaireResponseEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(QuestionnaireResponseEntity.class)
                .value("_id", "id")
                .dateAsInstant("_lastUpdated", "lastUpdated")
                .add(
                    TokenListMapping.<QuestionnaireResponseEntity>builder()
                        .parameterName("_tag")
                        .fieldName("metaTag")
                        .build())
                .value("author", "author")
                .dateAsInstant("authored", "authored")
                .value("questionnaire", "questionnaire")
                .value("source", "source")
                .value("subject", "subject")
                .get())
        .defaultQuery(returnNothing())
        .rules(
            List.of(
                atLeastOneParameterOf(
                    "_id",
                    "_lastUpdated",
                    "_tag",
                    "author",
                    "authored",
                    "questionnaire",
                    "source",
                    "subject"),
                ifParameter("_id")
                    .thenForbidParameters(
                        "_lastUpdated",
                        "_tag",
                        "author",
                        "authored",
                        "questionnaire",
                        "source",
                        "subject")))
        .build();
  }

  @PostMapping
  @Loggable(arguments = false)
  ResponseEntity<QuestionnaireResponse> create(
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse) {
    checkRequestState(
        isEmpty(questionnaireResponse.id()),
        "ID must be empty, found %s",
        questionnaireResponse.id());
    questionnaireResponse.id(generateRandomId());
    return create(questionnaireResponse, nowMillis());
  }

  /** Create resource. */
  public ResponseEntity<QuestionnaireResponse> create(
      QuestionnaireResponse questionnaireResponse, Instant now) {
    questionnaireResponse.meta(metaWithLastUpdated(questionnaireResponse.meta(), now));
    cim.applySource();
    repository.save(toEntity(questionnaireResponse));
    return ResponseEntity.created(
            URI.create(
                linkProperties.r4Url() + "/QuestionnaireResponse/" + questionnaireResponse.id()))
        .body(questionnaireResponse);
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @GetMapping(value = "/{id}")
  QuestionnaireResponse read(@PathVariable("id") String id) {
    return repository
        .findById(id)
        .map(e -> e.deserializePayload())
        .orElseThrow(() -> new Exceptions.NotFound(id));
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

  private VulcanizedBundler<
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

  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<QuestionnaireResponse> update(
      @PathVariable("id") String pathId,
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse) {
    checkRequestState(
        pathId.equals(questionnaireResponse.id()),
        "Path ID (%s) and request body ID (%s) do not match",
        pathId,
        questionnaireResponse.id());
    return update(questionnaireResponse, nowMillis());
  }

  ResponseEntity<QuestionnaireResponse> update(
      QuestionnaireResponse questionnaireResponse, Instant now) {
    questionnaireResponse.meta(metaWithLastUpdated(questionnaireResponse.meta(), now));
    cim.applySource();
    QuestionnaireResponseEntity entity =
        repository
            .findById(questionnaireResponse.id())
            .orElseThrow(() -> new Exceptions.NotFound(questionnaireResponse.id()));
    populateEntity(entity, questionnaireResponse);
    repository.save(entity);
    return ResponseEntity.ok(questionnaireResponse);
  }
}
