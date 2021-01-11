package gov.va.api.health.patientgenerateddata.questionnaire;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static gov.va.api.health.patientgenerateddata.Controllers.generateRandomId;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.net.URI;
import java.util.Optional;
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
    value = "/r4/Questionnaire",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class QuestionnaireController {
  private final LinkProperties linkProperties;

  private final QuestionnaireRepository repository;

  @PostMapping
  ResponseEntity<Questionnaire> create(@Valid @RequestBody Questionnaire questionnaire) {
    return create(generateRandomId(), questionnaire);
  }

  @SneakyThrows
  ResponseEntity<Questionnaire> create(String id, Questionnaire questionnaire) {
    checkRequestState(
        isEmpty(questionnaire.id()), "ID must be empty, found %s", questionnaire.id());
    questionnaire.id(id);
    QuestionnaireEntity entity = transform(questionnaire);
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

  QuestionnaireEntity transform(Questionnaire questionnaire) {
    return transform(questionnaire, QuestionnaireEntity.builder().build());
  }

  @SneakyThrows
  QuestionnaireEntity transform(Questionnaire questionnaire, QuestionnaireEntity entity) {
    return transform(
        questionnaire, entity, JacksonConfig.createMapper().writeValueAsString(questionnaire));
  }

  QuestionnaireEntity transform(
      @NonNull Questionnaire questionnaire, @NonNull QuestionnaireEntity entity, String payload) {
    entity.id(questionnaire.id());
    entity.payload(payload);
    return entity;
  }

  @SneakyThrows
  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Questionnaire> update(
      @PathVariable("id") String id, @Valid @RequestBody Questionnaire questionnaire) {
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaire);
    checkState(id.equals(questionnaire.id()), "%s != %s", id, questionnaire.id());
    Optional<QuestionnaireEntity> maybeEntity = repository.findById(id);
    QuestionnaireEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    entity = transform(questionnaire, entity, payload);
    repository.save(entity);
    return ResponseEntity.ok(questionnaire);
  }
}
