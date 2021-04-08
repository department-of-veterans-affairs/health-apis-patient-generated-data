package gov.va.api.health.patientgenerateddata;

import gov.va.api.health.patientgenerateddata.observation.ObservationRepository;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "/sandbox-data/r4",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
@ConditionalOnProperty(prefix = "sandbox-data-management", name = "enabled", havingValue = "true")
public class SandboxDeleteController {
  private final ObservationRepository observationRepository;

  private final QuestionnaireRepository questionnaireRepository;

  private final QuestionnaireResponseRepository questionnaireResponseRepository;

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
