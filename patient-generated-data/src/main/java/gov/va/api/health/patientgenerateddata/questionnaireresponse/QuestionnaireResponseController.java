package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.SerializationUtils.deserializedPayload;
import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.patientgenerateddata.ParseUtils;
import gov.va.api.health.patientgenerateddata.VulcanizedBundler;
import gov.va.api.health.r4.api.resources.Patient;
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
                .dateAsInstant("authored", "authored")
                .get())
        .defaultQuery(returnNothing())
        .rule(atLeastOneParameterOf("_id", "authored"))
        .rule(parametersNeverSpecifiedTogether("_id", "authored"))
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
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaireResponse);
    Instant authored = ParseUtils.parseDateTime(questionnaireResponse.authored());
    Optional<QuestionnaireResponseEntity> maybeEntity = repository.findById(id);
    if (maybeEntity.isPresent()) {
      QuestionnaireResponseEntity entity = maybeEntity.get();
      entity.payload(payload);
      entity.authored(authored);
      repository.save(entity);
      return ResponseEntity.ok(questionnaireResponse);
    }
    repository.save(
        QuestionnaireResponseEntity.builder().id(id).payload(payload).authored(authored).build());
    return ResponseEntity.created(
            URI.create(linkProperties.r4Url() + "r4/QuestionnaireResponse/" + id))
        .body(questionnaireResponse);
  }
}
