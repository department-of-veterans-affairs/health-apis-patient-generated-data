package gov.va.api.health.dataquery.service.controller.immunization;

import static gov.va.api.health.dataquery.service.controller.Dstu2Transformers.asReference;
import static gov.va.api.health.dataquery.service.controller.Transformers.asDateTimeString;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.immunization.DatamartImmunization.Status;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.DataAbsentReason.Reason;
import gov.va.api.health.dstu2.api.datatypes.Annotation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.resources.Immunization;
import java.util.List;
import java.util.Optional;
import lombok.Builder;

@Builder
public class Dstu2ImmunizationTransformer {

  private final DatamartImmunization datamart;

  List<Annotation> note(Optional<String> note) {
    return note.isPresent() ? List.of(Annotation.builder().text(note.get()).build()) : null;
  }

  List<Immunization.Reaction> reaction(Optional<DatamartReference> reaction) {
    return (reaction.isPresent() && reaction.get().hasDisplayOrTypeAndReference())
        ? List.of(Immunization.Reaction.builder().detail(asReference(reaction)).build())
        : null;
  }

  Immunization.Status status(DatamartImmunization.Status status) {
    if (status == null) {
      return null;
    }
    switch (status) {
      case completed:
        return Immunization.Status.completed;
      case entered_in_error:
        return Immunization.Status.entered_in_error;
      case data_absent_reason_unsupported:
        /* For unsupported, we'll need to set the _status extension field */
        return null;
      default:
        throw new IllegalArgumentException("Unsupported status: " + status);
    }
  }

  Extension statusExtension(Status status) {
    return status == Status.data_absent_reason_unsupported
        ? DataAbsentReason.of(Reason.unsupported)
        : null;
  }

  /** Convert the datamart structure to FHIR compliant structure. */
  public Immunization toFhir() {
    /*
     * vaccinationProtocol in the datamart model does not contain enough information to construct a
     * valid FHIR representation. It will be omitted.
     */
    return Immunization.builder()
        .resourceType(Immunization.class.getSimpleName())
        .id(datamart.cdwId())
        // .identifier() omitted since none are being emitted from CDW
        .status(status(datamart.status()))
        ._status(statusExtension(datamart.status()))
        .date(asDateTimeString(datamart.date()))
        .vaccineCode(vaccineCode(datamart.vaccineCode()))
        .patient(asReference(datamart.patient()))
        .wasNotGiven(datamart.wasNotGiven())
        .performer(asReference(datamart.performer()))
        .requester(asReference(datamart.requester()))
        .location(asReference(datamart.location()))
        ._reported(DataAbsentReason.of(Reason.unsupported))
        .note(note(datamart.note()))
        .reaction(reaction(datamart.reaction()))
        .build();
  }

  CodeableConcept vaccineCode(DatamartImmunization.VaccineCode vaccineCode) {
    if (vaccineCode == null) {
      return null;
    }
    return CodeableConcept.builder()
        .text(vaccineCode.text())
        .coding(
            List.of(
                Coding.builder()
                    .system("http://hl7.org/fhir/sid/cvx")
                    .code(vaccineCode.code())
                    .build()))
        .build();
  }
}
