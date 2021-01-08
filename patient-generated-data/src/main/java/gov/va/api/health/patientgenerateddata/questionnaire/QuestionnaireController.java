package gov.va.api.health.patientgenerateddata.questionnaire;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static gov.va.api.health.patientgenerateddata.Controllers.generateRandomId;
import static gov.va.api.lighthouse.vulcan.Rules.atLeastOneParameterOf;
import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.patientgenerateddata.VulcanizedBundler;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.UsageContext;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
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
    value = "/r4/Questionnaire",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class QuestionnaireController {
  private final LinkProperties linkProperties;

  private final QuestionnaireRepository repository;

  private VulcanConfiguration<QuestionnaireEntity> configuration() {
    return VulcanConfiguration.forEntity(QuestionnaireEntity.class)
        .paging(
            linkProperties.pagingConfiguration("Questionnaire", QuestionnaireEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(QuestionnaireEntity.class)
                // .composite("context-type-value", "xxx")
                .get())
        .defaultQuery(returnNothing())
        .rule(atLeastOneParameterOf("_id", "author", "authored", "subject"))
        .rule(ifParameter("_id").thenForbidParameters("author", "authored", "subject"))
        .build();
  }

  @PostMapping
  ResponseEntity<Questionnaire> create(@Valid @RequestBody Questionnaire questionnaire) {
    return create(generateRandomId(), questionnaire);
  }

  ResponseEntity<Questionnaire> create(String id, Questionnaire questionnaire) {
    checkRequestState(
        StringUtils.isEmpty(questionnaire.id()), "ID must be empty, found %s", questionnaire.id());
    questionnaire.id(id);
    return update(questionnaire.id(), questionnaire);
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
  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Questionnaire> update(
      @PathVariable("id") String id, @Valid @RequestBody Questionnaire questionnaire) {
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaire);
    checkState(id.equals(questionnaire.id()), "%s != %s", id, questionnaire.id());
    Optional<QuestionnaireEntity> maybeEntity = repository.findById(id);
    if (maybeEntity.isPresent()) {
      QuestionnaireEntity entity = maybeEntity.get();
      entity.payload(payload);
      entity.contextTypeValue(useContextValueJoin(questionnaire));
      repository.save(entity);
      return ResponseEntity.ok(questionnaire);
    }
    repository.save(
        QuestionnaireEntity.builder()
            .id(id)
            .payload(payload)
            .contextTypeValue(useContextValueJoin(questionnaire))
            .build());
    return ResponseEntity.created(URI.create(linkProperties.r4Url() + "/Questionnaire/" + id))
        .body(questionnaire);
  }

  static String useContextValueJoin(Questionnaire questionnaire) {
    if (questionnaire.useContext() == null) {
      return null;
    }
    return questionnaire
        .useContext()
        .stream()
        .flatMap(
            context -> {
              if (context.valueCodeableConcept() == null
                  || context.valueCodeableConcept().coding() == null) {
                return Stream.empty();
              }
              return context
                  .valueCodeableConcept()
                  .coding()
                  .stream()
                  .map(
                      valueCoding ->
                          Stream.of(codeJoin(context.code()), codeJoin(valueCoding))
                              .filter(StringUtils::isNotBlank)
                              .collect(joining("$")))
                  .filter(StringUtils::isNotBlank)
                  .map(join -> "|" + join + "|");
            })
        .collect(joining(","));
  }

  private static String codeJoin(Coding coding) {
    if (coding == null) {
      return null;
    }
    return trimToNull(
        Stream.of(coding.system(), coding.code())
            .filter(StringUtils::isNotBlank)
            .collect(joining("|")));
  }
}
