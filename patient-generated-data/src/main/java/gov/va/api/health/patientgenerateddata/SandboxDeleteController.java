package gov.va.api.health.patientgenerateddata;

import static com.google.common.base.Preconditions.checkState;

import gov.va.api.health.patientgenerateddata.observation.ObservationRepository;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(
    value = "/sandbox-data/r4",
    produces = {"application/json", "application/fhir+json"})
@ConditionalOnProperty(prefix = "sandbox-data-management", name = "enabled")
public class SandboxDeleteController {
  private final ObservationRepository observationRepository;

  private final QuestionnaireRepository questionnaireRepository;

  private final QuestionnaireResponseRepository questionnaireResponseRepository;

  SandboxDeleteController(
      @Value("${sandbox-data-management.enabled}") String enabled,
      ObservationRepository observationRepository,
      QuestionnaireRepository questionnaireRepository,
      QuestionnaireResponseRepository questionnaireResponseRepository) {
    checkState(
        "true".equalsIgnoreCase(enabled), "sandbox-data-management.enabled must be true or false");
    log.info("Using {}", getClass().getSimpleName());
    this.observationRepository = observationRepository;
    this.questionnaireRepository = questionnaireRepository;
    this.questionnaireResponseRepository = questionnaireResponseRepository;
  }

  @DeleteMapping(value = "/Observation/{id}")
  void deleteObservation(@PathVariable("id") String id) {
    var optionalObservation = observationRepository.findById(id);
    if (optionalObservation.isPresent()) {
      observationRepository.delete(optionalObservation.get());
    }
  }

  @DeleteMapping(value = "/Questionnaire/{id}")
  void deleteQuestionnaire(@PathVariable("id") String id) {
    var optionalQuestionnaire = questionnaireRepository.findById(id);
    if (optionalQuestionnaire.isPresent()) {
      questionnaireRepository.delete(optionalQuestionnaire.get());
    }
  }

  @DeleteMapping(value = "/QuestionnaireResponse/{id}")
  void deleteQuestionnaireResponse(@PathVariable("id") String id) {
    var optionalQuestionnaireResponse = questionnaireResponseRepository.findById(id);
    if (optionalQuestionnaireResponse.isPresent()) {
      questionnaireResponseRepository.delete(optionalQuestionnaireResponse.get());
    }
  }
}
