package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.patientgenerateddata.observation.ObservationController;
import gov.va.api.health.patientgenerateddata.patient.PatientController;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireController;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseController;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.Resource;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(
    value = "/management/r4",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class ManagementController {
  @Getter private final ObservationController observationController;

  @Getter private final PatientController patientController;

  @Getter private final QuestionnaireController questionnaireController;

  @Getter private final QuestionnaireResponseController questionnaireResponseController;

  @PostMapping(value = "/Observation")
  ResponseEntity<Observation> create(@Valid @RequestBody Observation observation) {
    String id = verifyAndGetId(observation, observationController.repository());
    observation.id(null);
    return observationController.create(id, observation);
  }

  @PostMapping(value = "/Patient")
  ResponseEntity<Patient> create(@Valid @RequestBody Patient patient) {
    // No additional processing, forward to controller
    return patientController.create(patient);
  }

  @PostMapping(value = "/Questionnaire")
  ResponseEntity<Questionnaire> create(@Valid @RequestBody Questionnaire questionnaire) {
    String id = verifyAndGetId(questionnaire, questionnaireController.repository());
    questionnaire.id(null);
    return questionnaireController.create(id, questionnaire);
  }

  @PostMapping(value = "/QuestionnaireResponse")
  ResponseEntity<QuestionnaireResponse> create(
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse) {
    String id = verifyAndGetId(questionnaireResponse, questionnaireResponseController.repository());
    questionnaireResponse.id(null);
    return questionnaireResponseController.create(id, questionnaireResponse);
  }

  <R extends Resource, T extends PayloadEntity<R>> String verifyAndGetId(
      R resource, CrudRepository<T, String> repository) {
    String id = resource.id();
    checkRequestState(!isBlank(id), "ID is required");
    if (repository.existsById(id)) {
      throw new Exceptions.AlreadyExists(String.format("Already exists %s", id));
    }
    return id;
  }
}
