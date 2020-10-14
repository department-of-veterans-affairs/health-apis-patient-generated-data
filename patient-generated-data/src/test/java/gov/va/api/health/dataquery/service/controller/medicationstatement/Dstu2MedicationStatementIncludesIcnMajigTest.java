package gov.va.api.health.dataquery.service.controller.medicationstatement;

import gov.va.api.health.dataquery.service.controller.ExtractIcnValidator;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.MedicationStatement;
import java.util.List;
import org.junit.jupiter.api.Test;

public class Dstu2MedicationStatementIncludesIcnMajigTest {
  @Test
  public void extractIcn() {
    ExtractIcnValidator.builder()
        .majig(new Dstu2MedicationStatementIncludesIcnMajig())
        .body(
            MedicationStatement.builder()
                .id("123")
                .patient(Reference.builder().reference("Patient/1010101010V666666").build())
                .build())
        .expectedIcns(List.of("1010101010V666666"))
        .build()
        .assertIcn();
  }
}
