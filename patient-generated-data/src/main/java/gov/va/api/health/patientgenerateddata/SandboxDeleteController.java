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

@RestController
@RequestMapping(
    value = "/sandbox-data/r4",
    produces = {"application/json", "application/fhir+json"})
@ConditionalOnProperty(prefix = "sandbox-data-management", name = "enabled")
@Slf4j
public class SandboxDeleteController {
  private final ObservationRepository observationRepository;

  private final QuestionnaireRepository questionnaireRepository;

  private final QuestionnaireResponseRepository questionnaireResponseRepository;

  /** Constructor to inject the repositories, and validate the enabled property with booleans. */
  public SandboxDeleteController(
      @Value("${sandbox-data-management.enabled}") String isSandboxEnabled,
      ObservationRepository observationRepository,
      QuestionnaireRepository questionnaireRepository,
      QuestionnaireResponseRepository questionnaireResponseRepository) {
    checkState("true".equals(isSandboxEnabled) || "false".equals(isSandboxEnabled));
    log.info("Sandbox Data Management Enabled: {}", isSandboxEnabled);
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
