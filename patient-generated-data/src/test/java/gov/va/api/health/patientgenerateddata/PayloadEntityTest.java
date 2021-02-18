package gov.va.api.health.patientgenerateddata;

import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.va.api.health.patientgenerateddata.patient.PatientEntity;
import org.junit.jupiter.api.Test;

public class PayloadEntityTest {
  @Test
  void badPayload() {
    PatientEntity entity = PatientEntity.builder().id("x").payload("notjson").build();
    assertThrows(Exceptions.InvalidPayload.class, () -> entity.deserializePayload());
  }
}
