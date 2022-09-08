package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static gov.va.api.health.patientgenerateddata.Controllers.nowMillis;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.patientgenerateddata.observation.ObservationController;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireController;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseController;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(
    value = "/management",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ManagementController {
  private final ObservationController observationController;

  private final QuestionnaireController questionnaireController;

  private final QuestionnaireResponseController questionnaireResponseController;

  private static void validateId(String id, Function<String, Optional<?>> finder) {
    checkRequestState(!isBlank(id), "ID is required");
    if (finder.apply(id).isPresent()) {
      throw new Exceptions.AlreadyExists(String.format("ID %s already exists", id));
    }
  }

  @GetMapping(value = "/archive/r4/QuestionnaireResponse/{id}")
  QuestionnaireResponse archivedQuestionnaireResponse(@PathVariable("id") String id) {
    return questionnaireResponseController.findArchivedById(id).orElse(null);
  }

  @PostMapping(value = "/r4/Observation")
  ResponseEntity<Observation> create(
      @Valid @RequestBody Observation observation,
      @RequestHeader(name = "Authorization", required = true) String authorization) {
    validateId(observation.id(), id -> observationController.findById(id));
    return create(observation, authorization, nowMillis());
  }

  ResponseEntity<Observation> create(Observation observation, String authorization, Instant now) {
    return observationController.create(observation, authorization, null, now);
  }

  @PostMapping(value = "/r4/Questionnaire")
  ResponseEntity<Questionnaire> create(
      @Valid @RequestBody Questionnaire questionnaire,
      @RequestHeader(name = "Authorization", required = true) String authorization) {
    validateId(questionnaire.id(), id -> questionnaireController.findById(id));
    return create(questionnaire, authorization, nowMillis());
  }

  ResponseEntity<Questionnaire> create(
      Questionnaire questionnaire, String authorization, Instant now) {
    return questionnaireController.create(questionnaire, authorization, null, now);
  }

  @PostMapping(value = "/r4/QuestionnaireResponse")
  ResponseEntity<QuestionnaireResponse> create(
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse,
      @RequestHeader(name = "Authorization", required = true) String authorization) {
    validateId(questionnaireResponse.id(), id -> questionnaireResponseController.findById(id));
    return create(questionnaireResponse, authorization, nowMillis());
  }

  ResponseEntity<QuestionnaireResponse> create(
      QuestionnaireResponse questionnaireResponse, String authorization, Instant now) {
    return questionnaireResponseController.create(questionnaireResponse, authorization, null, now);
  }

  @GetMapping(value = "/r4/Observation/ids")
  List<String> observationIds() {
    return observationController.getAllIds();
  }

  @DeleteMapping(value = "/archive/r4/purge")
  List<String> purgeArchives() {
    return questionnaireResponseController.purgeArchives();
  }

  @GetMapping(value = "/r4/Questionnaire/ids")
  List<String> questionnaireIds() {
    return questionnaireController.getAllIds();
  }

  @GetMapping(value = "/r4/QuestionnaireResponse/ids")
  List<String> questionnaireResponseIds() {
    return questionnaireResponseController.getAllIds();
  }
}
