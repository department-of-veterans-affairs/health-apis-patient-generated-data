package gov.va.api.health.patientgenerateddata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

  @Test
  void deleteInvalid() {
    var observationRepo = mock(ObservationRepository.class);
    var questionnaireRepo = mock(QuestionnaireRepository.class);
    var questionnaireResponseRepo = mock(QuestionnaireResponseRepository.class);
    var controller =
        new SandboxDeleteController(observationRepo, questionnaireRepo, questionnaireResponseRepo);
    controller.deleteObservation("y");
    controller.deleteQuestionnaire("y");
    controller.deleteQuestionnaireResponse("y");
  }

  @Test
  void deleteObservation() {
    var observationRepo = mock(ObservationRepository.class);
    var questionnaireRepo = mock(QuestionnaireRepository.class);
    var questionnaireResponseRepo = mock(QuestionnaireResponseRepository.class);
    when(observationRepo.findById("x"))
        .thenReturn(Optional.of(ObservationEntity.builder().id("x").build()));
    var controller =
        new SandboxDeleteController(observationRepo, questionnaireRepo, questionnaireResponseRepo);
    controller.deleteObservation("x");
    verify(observationRepo).delete(any(ObservationEntity.class));
  }

  @Test
  void deleteQuestionnaire() {
    var observationRepo = mock(ObservationRepository.class);
    var questionnaireRepo = mock(QuestionnaireRepository.class);
    var questionnaireResponseRepo = mock(QuestionnaireResponseRepository.class);
    when(questionnaireRepo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireEntity.builder().id("x").build()));
    var controller =
        new SandboxDeleteController(observationRepo, questionnaireRepo, questionnaireResponseRepo);
    controller.deleteQuestionnaire("x");
    verify(questionnaireRepo).delete(any(QuestionnaireEntity.class));
  }

  @Test
  void deleteQuestionnaireResponse() {
    var observationRepo = mock(ObservationRepository.class);
    var questionnaireRepo = mock(QuestionnaireRepository.class);
    var questionnaireResponseRepo = mock(QuestionnaireResponseRepository.class);
    when(questionnaireResponseRepo.findById("x"))
        .thenReturn(Optional.of(QuestionnaireResponseEntity.builder().id("x").build()));
    var controller =
        new SandboxDeleteController(observationRepo, questionnaireRepo, questionnaireResponseRepo);
    controller.deleteQuestionnaireResponse("x");
    verify(questionnaireResponseRepo).delete(any(QuestionnaireResponseEntity.class));
  }
}
