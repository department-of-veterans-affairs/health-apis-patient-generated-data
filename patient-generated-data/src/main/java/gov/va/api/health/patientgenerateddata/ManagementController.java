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
import java.util.Optional;
import java.util.function.Function;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(
    value = "/management/r4",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ManagementController {
  private final ObservationController observationController;

  private final QuestionnaireController questionnaireController;

  private final QuestionnaireResponseController questionnaireResponseController;

  private static void validateId(String id, Function<String, Optional<?>> f) {
    checkRequestState(!isBlank(id), "ID is required");
    if (f.apply(id).isPresent()) {
      throw new Exceptions.AlreadyExists(String.format("ID %s already exists", id));
    }
  }

  @PostMapping(value = "/Observation")
  ResponseEntity<Observation> create(
      @Valid @RequestBody Observation observation,
      @RequestHeader(name = "Authorization") String authorization) {
    validateId(observation.id(), id -> observationController.findById(id));
    return observationController.create(observation, authorization, nowMillis());
  }

  @PostMapping(value = "/Questionnaire")
  ResponseEntity<Questionnaire> create(@Valid @RequestBody Questionnaire questionnaire) {
    validateId(questionnaire.id(), id -> questionnaireController.findById(id));
    return questionnaireController.create(questionnaire, nowMillis());
  }

  @PostMapping(value = "/QuestionnaireResponse")
  ResponseEntity<QuestionnaireResponse> create(
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse) {
    validateId(questionnaireResponse.id(), id -> questionnaireResponseController.findById(id));
    return questionnaireResponseController.create(questionnaireResponse, nowMillis());
  }
}
