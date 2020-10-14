package gov.va.api.health.dataquery.service.controller.patient;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dstu2.api.resources.Patient;
import org.junit.jupiter.api.Test;

public class Dstu2PatientTransformerTest {
  @Test
  public void empty() {
    assertThat(
            Dstu2PatientTransformer.builder()
                .datamart(DatamartPatient.builder().build())
                .build()
                .toFhir())
        .isEqualTo(Patient.builder().resourceType("Patient").build());
  }
}
