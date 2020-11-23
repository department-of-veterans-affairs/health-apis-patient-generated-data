package gov.va.api.health.patientgenerateddata.questionnaire;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.ReferenceQualifier.stream;
import static gov.va.api.health.patientgenerateddata.SerializationUtils.deserializedPayload;

import com.google.common.collect.Streams;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.ReferenceQualifier;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
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
    value = "/r4/Questionnaire",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class QuestionnaireController {
  private final QuestionnaireRepository repository;

  private final ReferenceQualifier referenceQualifier;

  private static Stream<Reference> references(Questionnaire.Item item) {
    if (item == null) {
      return Stream.empty();
    }
    return Streams.concat(
            stream(item.enableWhen()).map(e -> e.answerReference()),
            stream(item.answerOption()).map(a -> a.valueReference()),
            stream(item.initial()).map(i -> i.valueReference()),
            stream(item.item()).flatMap(i -> references(i)))
        .filter(Objects::nonNull);
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @GetMapping(value = "/{id}")
  Questionnaire read(@PathVariable("id") String id) {
    Optional<QuestionnaireEntity> maybeEntity = repository.findById(id);
    QuestionnaireEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    Questionnaire fhir = deserializedPayload(id, entity.payload(), Questionnaire.class);
    Stream<Reference> references = stream(fhir.item()).flatMap(i -> references(i));
    referenceQualifier.qualify(references);
    return fhir;
  }

  @SneakyThrows
  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Void> update(
      @PathVariable("id") String id, @Valid @RequestBody Questionnaire questionnaire) {
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaire);
    checkState(id.equals(questionnaire.id()), "%s != %s", id, questionnaire.id());
    Optional<QuestionnaireEntity> maybeEntity = repository.findById(id);
    if (maybeEntity.isPresent()) {
      QuestionnaireEntity entity = maybeEntity.get();
      entity.payload(payload);
      repository.save(entity);
      return ResponseEntity.ok().build();
    }
    repository.save(QuestionnaireEntity.builder().id(id).payload(payload).build());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
