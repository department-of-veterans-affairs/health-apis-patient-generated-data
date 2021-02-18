package gov.va.api.health.patientgenerateddata;

import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireEntity;
import org.junit.jupiter.api.Test;

public class PayloadEntityTest {
  @Test
  void badPayload() {
    QuestionnaireEntity entity = QuestionnaireEntity.builder().id("x").payload("notjson").build();
    assertThrows(Exceptions.InvalidPayload.class, () -> entity.deserializePayload());
  }
}
