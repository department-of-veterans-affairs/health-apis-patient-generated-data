package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static gov.va.api.health.patientgenerateddata.Controllers.nowMillis;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.patientgenerateddata.observation.ObservationController;
import gov.va.api.health.patientgenerateddata.observation.ObservationRepository;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireController;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseController;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.Resource;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
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
  private final LinkProperties linkProperties;

  private final Sourcerer sourcerer;

  private final ObservationRepository observationRepository;

  private final QuestionnaireRepository questionnaireRepository;

  private final QuestionnaireResponseRepository questionnaireResponseRepository;

  @PostMapping(value = "/Observation")
  ResponseEntity<Observation> create(
      @Valid @RequestBody Observation observation,
      @RequestHeader(name = "Authorization", required = true) String authorization) {
    validateId(observation, observationRepository);
    return new ObservationController(linkProperties, observationRepository, sourcerer)
        .create(observation, authorization, nowMillis());
  }

  @PostMapping(value = "/Questionnaire")
  ResponseEntity<Questionnaire> create(@Valid @RequestBody Questionnaire questionnaire) {
    validateId(questionnaire, questionnaireRepository);
    return new QuestionnaireController(linkProperties, questionnaireRepository, sourcerer)
        .create(questionnaire, nowMillis());
  }

  @PostMapping(value = "/QuestionnaireResponse")
  ResponseEntity<QuestionnaireResponse> create(
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse) {
    validateId(questionnaireResponse, questionnaireResponseRepository);
    return new QuestionnaireResponseController(
            linkProperties, questionnaireResponseRepository, sourcerer)
        .create(questionnaireResponse, nowMillis());
  }

  <R extends Resource, T extends PayloadEntity<R>> void validateId(
      R resource, CrudRepository<T, String> repository) {
    String id = resource.id();
    checkRequestState(!isBlank(id), "ID is required");
    if (repository.existsById(id)) {
      throw new Exceptions.AlreadyExists(String.format("ID %s already exists", id));
    }
  }
}
