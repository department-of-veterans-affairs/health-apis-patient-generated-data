package gov.va.api.health.patientgenerateddata;

import gov.va.api.health.patientgenerateddata.observation.ObservationRepository;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "/sandbox-data/r4",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ResourceDeleteController {

  @Getter private final ObservationRepository observationRepository;

  @Getter private final QuestionnaireRepository questionnaireRepository;

  @Getter private final QuestionnaireResponseRepository questionnaireResponseRepository;

  @PostMapping(value = "/Observation/{id}")
  void deleteObservation(@PathVariable("id") String id) {
    var optionalObservation = observationRepository.findById(id);
    var observation = optionalObservation.orElseGet(null);
    if (observation == null) {
      return;
    }
    observationRepository.delete(observation);
  }

  @PostMapping(value = "/Questionnaire/{id}")
  void deleteQuestionnaire(@PathVariable("id") String id) {
    var optionalQuestionnaire = questionnaireRepository.findById(id);
    var questionnaire = optionalQuestionnaire.orElseGet(null);
    if (questionnaire == null) {
      return;
    }
    questionnaireRepository.delete(questionnaire);
  }

  @PostMapping(value = "/QuestionnaireResponse/{id}")
  void deleteQuestionnaireResponse(@PathVariable("id") String id) {
    var optionalQuestionnaireResponse = questionnaireResponseRepository.findById(id);
    var questionnaireResponse = optionalQuestionnaireResponse.orElseGet(null);
    if (questionnaireResponse == null) {
      return;
    }
    questionnaireResponseRepository.delete(questionnaireResponse);
  }
}
