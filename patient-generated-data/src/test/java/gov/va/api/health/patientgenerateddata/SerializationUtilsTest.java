package gov.va.api.health.patientgenerateddata;

import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import org.junit.jupiter.api.Test;

public class SerializationUtilsTest {

  @Test
  void badPayload() {
    assertThrows(
        Exceptions.InvalidPayload.class,
        () -> SerializationUtils.deserializedPayload("x", "notjson", QuestionnaireResponse.class));
  }
}
