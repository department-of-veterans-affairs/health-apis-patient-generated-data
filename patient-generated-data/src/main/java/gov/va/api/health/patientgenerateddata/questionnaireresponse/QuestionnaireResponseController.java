package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.SerializationUtils.deserializedPayload;
import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.ParseUtils;
import gov.va.api.health.patientgenerateddata.vulcanizer.Bundling;
import gov.va.api.health.patientgenerateddata.vulcanizer.LinkProperties;
import gov.va.api.health.patientgenerateddata.vulcanizer.VulcanizedBundler;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse.Bundle;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.time.Instant;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    value = "/r4/QuestionnaireResponse",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class QuestionnaireResponseController {
  private final LinkProperties linkProperties;

  private final QuestionnaireResponseRepository repository;

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
                .get())
        .defaultQuery(returnNothing())
        .rule(atLeastOneParameterOf("_id", "authored", "author"))
        .build();
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @GetMapping(value = "/{id}")
  QuestionnaireResponse read(@PathVariable("id") String id) {
    Optional<QuestionnaireResponseEntity> maybeEntity = repository.findById(id);
    QuestionnaireResponseEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    return deserializedPayload(id, entity.payload(), QuestionnaireResponse.class);
  }

  /** QuestionnaireResponse Search. */
  @GetMapping
  public QuestionnaireResponse.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          QuestionnaireResponseEntity, QuestionnaireResponse, QuestionnaireResponse.Entry, Bundle>
      toBundle() {
    return VulcanizedBundler.forBundling(
            QuestionnaireResponseEntity.class,
            Bundling.newBundle(QuestionnaireResponse.Bundle::new)
                .newEntry(QuestionnaireResponse.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .toResource(QuestionnaireResponseEntity::deserializePayload)
        .build();
  }

  @SneakyThrows
  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Void> update(
      @PathVariable("id") String id,
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse) {
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaireResponse);
    checkState(id.equals(questionnaireResponse.id()), "%s != %s", id, questionnaireResponse.id());
    Reference author = questionnaireResponse.author();
    Instant authored = ParseUtils.parseDateTime(questionnaireResponse.authored());
    Optional<QuestionnaireResponseEntity> maybeEntity = repository.findById(id);
    if (maybeEntity.isPresent()) {
      QuestionnaireResponseEntity entity = maybeEntity.get();
      entity.payload(payload);
      entity.author(author.id());
      entity.authored(authored);
      repository.save(entity);
      return ResponseEntity.ok().build();
    }
    repository.save(
        QuestionnaireResponseEntity.builder()
            .id(id)
            .payload(payload)
            .author(author.id())
            .authored(authored)
            .build());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
