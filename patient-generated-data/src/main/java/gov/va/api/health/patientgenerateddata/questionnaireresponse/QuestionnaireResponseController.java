package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.SerializationUtils.deserializedPayload;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.util.Optional;
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

  private final QuestionnaireResponseRepository repository;

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

  @SneakyThrows
  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Void> update(
      @PathVariable("id") String id,
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse) {
    String payload = JacksonConfig.createMapper().writeValueAsString(questionnaireResponse);
    checkState(id.equals(questionnaireResponse.id()), "%s != %s", id, questionnaireResponse.id());
    Optional<QuestionnaireResponseEntity> maybeEntity = repository.findById(id);
    if (maybeEntity.isPresent()) {
      QuestionnaireResponseEntity entity = maybeEntity.get();
      entity.payload(payload);
      repository.save(entity);
      return ResponseEntity.ok().build();
    }
    repository.save(QuestionnaireResponseEntity.builder().id(id).payload(payload).build());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
