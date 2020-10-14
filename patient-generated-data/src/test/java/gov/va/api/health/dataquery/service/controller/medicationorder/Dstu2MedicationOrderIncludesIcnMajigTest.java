package gov.va.api.health.dataquery.service.controller.medicationorder;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.MedicationOrder;
import java.util.List;
import org.junit.jupiter.api.Test;

public class Dstu2MedicationOrderIncludesIcnMajigTest {
  @Test
  public void extractIcn() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2MedicationOrderIncludesIcnMajig())
        .body(
            MedicationOrder.builder()
                .id("123")
                .patient(Reference.builder().reference("Patient/1010101010V666666").build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
