package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;
import static gov.va.api.health.dataquery.service.controller.Transformers.isBlank;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static java.util.Collections.singletonList;

import gov.va.api.health.dataquery.service.controller.Dstu2Transformers;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.DiagnosticReport;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class Dstu2DiagnosticReportTransformer {
  @NonNull final DatamartDiagnosticReport datamart;

  private CodeableConcept category() {
    return CodeableConcept.builder()
        .coding(
            singletonList(
                Coding.builder()
                    .system("http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                    .code("LAB")
                    .display("Laboratory")
                    .build()))
        .build();
  }

  private CodeableConcept code() {
    return CodeableConcept.builder().text("panel").build();
  }

  private Reference performer() {
    return datamart.accessionInstitution().isPresent()
        ? asReference(datamart.accessionInstitution())
        : null;
  }

  private Extension performerExtension() {
    return datamart.accessionInstitution().isEmpty()
        ? DataAbsentReason.of(DataAbsentReason.Reason.unknown)
        : null;
  }

  private List<Reference> results() {
    if (isBlank(datamart.results())) {
      return null;
    }
    return datamart.results().stream()
        .map(Dstu2Transformers::asReference)
        .collect(Collectors.toList());
  }

  DiagnosticReport toFhir() {
    return DiagnosticReport.builder()
        .id(datamart.cdwId())
        .resourceType("DiagnosticReport")
        .status(DiagnosticReport.Code._final)
        .category(category())
        .code(code())
        .subject(asReference(datamart.patient()))
        .effectiveDateTime(transformDateTime(datamart.effectiveDateTime()))
        .issued(transformDateTime(datamart.issuedDateTime()))
        .performer(performer())
        ._performer(performerExtension())
        .result(results())
        .build();
  }

  /** Transforms a Datamart DateTime string to a Fhir DateTime string. */
  String transformDateTime(String maybeDateTime) {
    return asDateTimeString(parseInstant(maybeDateTime));
  }
}
