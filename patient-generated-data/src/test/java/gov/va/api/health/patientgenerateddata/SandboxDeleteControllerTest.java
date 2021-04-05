package gov.va.api.health.patientgenerateddata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.patientgenerateddata.observation.ObservationEntity;
import gov.va.api.health.patientgenerateddata.observation.ObservationRepository;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireEntity;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseEntity;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class SandboxDeleteControllerTest {

  private SandboxDeleteController controller() {
    var observationRepo = mock(ObservationRepository.class);
    var questionnaireRepo = mock(QuestionnaireRepository.class);
    var questionnaireResponseRepo = mock(QuestionnaireResponseRepository.class);
    when(observationRepo.findById("x"))
        .thenReturn(Optional.of(ObservationEntity.builder().id("x").build()));
    when(questionnaireRepo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").build()));
    when(questionnaireResponseRepo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireResponseEntity.builder().id("x").build()));
    return new SandboxDeleteController(
        observationRepo, questionnaireRepo, questionnaireResponseRepo);
  }

  @Test
  void deleteObservation() {
    var controller = controller();
    controller.deleteObservation("x");
  }

  @Test
  void deleteQuestionnaire() {
    var controller = controller();
    controller.deleteQuestionnaire("x");
  }

  @Test
  void deleteQuestionnaireResponse() {
    var controller = controller();
    controller.deleteQuestionnaireResponse("x");
  }
}
