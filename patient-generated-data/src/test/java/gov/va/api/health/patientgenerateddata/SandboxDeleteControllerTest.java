package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
  void catchUnsetProperty() {
    var observationRepo = mock(ObservationRepository.class);
    var questionnaireRepo = mock(QuestionnaireRepository.class);
    var questionnaireResponseRepo = mock(QuestionnaireResponseRepository.class);
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                new SandboxDeleteController(
                    "unset", observationRepo, questionnaireRepo, questionnaireResponseRepo));
  }

  SandboxDeleteController defaultMockedController() {
    var observationRepo = mock(ObservationRepository.class);
    var questionnaireRepo = mock(QuestionnaireRepository.class);
    var questionnaireResponseRepo = mock(QuestionnaireResponseRepository.class);
    return new SandboxDeleteController(
        "true", observationRepo, questionnaireRepo, questionnaireResponseRepo);
  }

  @Test
  void deleteInvalidObservation() {
    var controller = defaultMockedController();
    controller.deleteObservation("y");
  }

  @Test
  void deleteInvalidQuestionnaire() {
    var controller = defaultMockedController();
    controller.deleteQuestionnaire("y");
  }

  @Test
  void deleteInvalidQuestionnaireResponse() {
    var controller = defaultMockedController();
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
        new SandboxDeleteController(
            "true", observationRepo, questionnaireRepo, questionnaireResponseRepo);
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
        new SandboxDeleteController(
            "true", observationRepo, questionnaireRepo, questionnaireResponseRepo);
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
        new SandboxDeleteController(
            "true", observationRepo, questionnaireRepo, questionnaireResponseRepo);
    controller.deleteQuestionnaireResponse("x");
    verify(questionnaireResponseRepo).delete(any(QuestionnaireResponseEntity.class));
  }
}
