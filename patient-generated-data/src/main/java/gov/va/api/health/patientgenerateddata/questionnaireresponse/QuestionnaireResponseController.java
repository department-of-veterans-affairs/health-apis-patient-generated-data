package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static gov.va.api.health.patientgenerateddata.Controllers.generateRandomId;
import static gov.va.api.health.patientgenerateddata.Controllers.lastUpdatedFromMeta;
import static gov.va.api.health.patientgenerateddata.Controllers.matchIcn;
import static gov.va.api.health.patientgenerateddata.Controllers.metaWithLastUpdatedAndSource;
import static gov.va.api.health.patientgenerateddata.Controllers.nowMillis;
import static gov.va.api.health.patientgenerateddata.Controllers.parseDateTime;
import static gov.va.api.health.patientgenerateddata.Controllers.resourceId;
import static gov.va.api.health.patientgenerateddata.Controllers.validateSource;
import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.IncludesIcnMajig;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    value = "/r4/QuestionnaireResponse",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class QuestionnaireResponseController {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private final LinkProperties linkProperties;

  private final ArchivedQuestionnaireResponseRepository archivedRepository;

  private final QuestionnaireResponseRepository repository;

  private final Sourcerer sourcerer;

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

  /** Archive the QuestionnaireResponse if it exists and then delete it from the main table. */
  @DeleteMapping(value = "/{id}")
  ResponseEntity<Void> archivedDelete(
      @PathVariable("id") String id,
      @RequestHeader(name = "x-va-icn", required = false) String icn) {
    ResponseEntity<Void> response =
        ResponseEntity.status(HttpStatus.NO_CONTENT)
            .header(IncludesIcnMajig.INCLUDES_ICN_HEADER, isBlank(icn) ? "NONE" : icn)
            .body(null);
    var optionalQuestionnaireResponse = repository.findById(id);
    if (optionalQuestionnaireResponse.isEmpty()) {
      return response;
    }
    var questionnaireResponse = optionalQuestionnaireResponse.get();
    matchIcn(
        icn,
        questionnaireResponse.deserializePayload(),
        QuestionnaireResponseIncludesIcnMajig::icns);
    ArchivedQuestionnaireResponseEntity archivedEntity =
        ArchivedQuestionnaireResponseEntity.builder()
            .id(questionnaireResponse.id())
            .payload(questionnaireResponse.payload())
            .deletionTimestamp(nowMillis())
            .build();
    archivedRepository.save(archivedEntity);
    repository.delete(questionnaireResponse);
    return response;
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
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse,
      @RequestHeader(name = "Authorization", required = true) String authorization,
      @RequestHeader(name = "x-va-icn", required = false) String icn) {
    checkRequestState(
        isEmpty(questionnaireResponse.id()),
        "ID must be empty, found %s",
        questionnaireResponse.id());
    questionnaireResponse.id(generateRandomId());
    return create(questionnaireResponse, authorization, icn, nowMillis());
  }

  /** Create resource. */
  public ResponseEntity<QuestionnaireResponse> create(
      QuestionnaireResponse questionnaireResponse, String authorization, String icn, Instant now) {
    matchIcn(icn, questionnaireResponse, QuestionnaireResponseIncludesIcnMajig::icns);
    questionnaireResponse.meta(
        metaWithLastUpdatedAndSource(
            questionnaireResponse.meta(), now, sourcerer.source(authorization)));
    repository.save(toEntity(questionnaireResponse));
    return ResponseEntity.created(
            URI.create(
                linkProperties.r4Url() + "/QuestionnaireResponse/" + questionnaireResponse.id()))
        .body(questionnaireResponse);
  }

  public Optional<QuestionnaireResponse> findArchivedById(String id) {
    return archivedRepository.findById(id).map(e -> e.deserializePayload());
  }

  public Optional<QuestionnaireResponse> findById(String id) {
    return repository.findById(id).map(e -> e.deserializePayload());
  }

  /** Get all IDs for QuestionnaireResponse resource. */
  public List<String> getAllIds() {
    return Streams.stream(repository.findAll()).map(e -> e.id()).sorted().collect(toList());
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  /** Remove 5 year or older archived questionnaire responses. */
  public List<String> purgeArchives() {
    Instant fiveYearsAgo = ZonedDateTime.now(ZoneId.of("UTC")).minusYears(5).toInstant();
    List<String> deletedArchives = new ArrayList<>();
    Streams.stream(archivedRepository.findAll())
        .filter(entity -> entity.deletionTimestamp().isBefore(fiveYearsAgo))
        .forEach(
            entity -> {
              deletedArchives.add(entity.id());
              archivedRepository.delete(entity);
            });
    return deletedArchives;
  }

  @GetMapping(value = "/{id}")
  public QuestionnaireResponse read(@PathVariable("id") String id) {
    return findById(id).orElseThrow(() -> new Exceptions.NotFound(id));
  }

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
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse,
      @RequestHeader(name = "Authorization", required = true) String authorization,
      @RequestHeader(name = "x-va-icn", required = false) String icn) {
    checkRequestState(
        pathId.equals(questionnaireResponse.id()),
        "Path ID (%s) and request body ID (%s) do not match",
        pathId,
        questionnaireResponse.id());
    return update(questionnaireResponse, authorization, icn, nowMillis());
  }

  /** Update the given resource. */
  public ResponseEntity<QuestionnaireResponse> update(
      QuestionnaireResponse questionnaireResponse, String authorization, String icn, Instant now) {
    matchIcn(icn, questionnaireResponse, QuestionnaireResponseIncludesIcnMajig::icns);
    String authorizationSource = sourcerer.source(authorization);
    QuestionnaireResponseEntity entity =
        repository
            .findById(questionnaireResponse.id())
            .orElseThrow(() -> new Exceptions.NotFound(questionnaireResponse.id()));
    validateSource(
        questionnaireResponse.id(),
        authorizationSource,
        entity.deserializePayload().meta().source());
    questionnaireResponse.meta(
        metaWithLastUpdatedAndSource(questionnaireResponse.meta(), now, authorizationSource));
    populateEntity(entity, questionnaireResponse);
    repository.save(entity);
    return ResponseEntity.ok(questionnaireResponse);
  }
}
