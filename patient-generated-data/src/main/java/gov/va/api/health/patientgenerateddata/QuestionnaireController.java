package gov.va.api.health.patientgenerateddata;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// import gov.va.api.health.dataquery.service.controller.CountParameter;
// import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
// import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
// import gov.va.api.health.dataquery.service.controller.PageLinks;
// import gov.va.api.health.dataquery.service.controller.Parameters;
// import gov.va.api.health.dataquery.service.controller.R4Bundler;
// import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
// import gov.va.api.health.dataquery.service.controller.TokenParameter;
// import gov.va.api.health.dataquery.service.controller.WitnessProtection;
@Validated
@RestController
@RequestMapping(
    value = {"/r4/Questionnaire"},
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class QuestionnaireController {
  private final QuestionnaireRepository repository;

  @SneakyThrows
  private static <T> T deserializedPayload(
      @NonNull String id, @NonNull String payload, @NonNull Class<T> clazz) {
    try {
      return JacksonConfig.createMapper().readValue(payload, clazz);
    } catch (Exception e) {
      throw new Exceptions.InvalidPayload(id, e);
    }
  }

  @GetMapping(value = "/{id}")
  Questionnaire read(@PathVariable("id") String id) {
    Optional<QuestionnaireEntity> maybeEntity = repository.findById(id);
    QuestionnaireEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    return deserializedPayload(id, entity.payload(), Questionnaire.class);
  }
}
