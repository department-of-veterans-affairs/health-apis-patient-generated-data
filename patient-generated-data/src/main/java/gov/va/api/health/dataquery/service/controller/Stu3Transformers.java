package gov.va.api.health.dataquery.service.controller;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.stu3.api.datatypes.CodeableConcept;
import gov.va.api.health.stu3.api.datatypes.Coding;
import gov.va.api.health.stu3.api.elements.Reference;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;

/** Utility methods for STU3 transformers. */
@UtilityClass
public final class Stu3Transformers {
  /**
   * Convert the coding to a FHIR coding and wrap it with a codeable concept. Returns null of it
   * cannot be converted.
   */
  public static CodeableConcept asCodeableConceptWrapping(DatamartCoding coding) {
    Coding fhirCoding = asCoding(coding);
    if (fhirCoding == null) {
      return null;
    }
    return CodeableConcept.builder().coding(List.of(fhirCoding)).build();
  }

  /** Convert the Optional datamart coding to Fhir, otherwise return null. */
  public static CodeableConcept asCodeableConceptWrapping(Optional<DatamartCoding> coding) {
    if (coding == null || coding.isEmpty() || asCoding(coding.get()) == null) {
      return null;
    }
    return CodeableConcept.builder().coding(List.of(asCoding(coding.get()))).build();
  }

  /** Convert the datamart coding to coding if possible, otherwise return null. */
  public static Coding asCoding(Optional<DatamartCoding> maybeCoding) {
    if (maybeCoding == null || maybeCoding.isEmpty()) {
      return null;
    }
    return asCoding(maybeCoding.get());
  }

  /** Convert the datamart coding to coding if possible, otherwise return null. */
  public static Coding asCoding(DatamartCoding datamartCoding) {
    if (datamartCoding == null || !datamartCoding.hasAnyValue()) {
      return null;
    }
    return Coding.builder()
        .system(datamartCoding.system().orElse(null))
        .code(datamartCoding.code().orElse(null))
        .display(datamartCoding.display().orElse(null))
        .build();
  }

  /** Convert the datamart reference (if specified) to a FHIR reference. */
  public static Reference asReference(Optional<DatamartReference> maybeReference) {
    if (maybeReference == null || maybeReference.isEmpty()) {
      return null;
    }
    return asReference(maybeReference.get());
  }

  /** Convert the datamart reference (if specified) to a FHIR reference. */
  public static Reference asReference(DatamartReference maybeReference) {
    if (maybeReference == null) {
      return null;
    }
    Optional<String> path = maybeReference.asRelativePath();
    if (maybeReference.display().isEmpty() && path.isEmpty()) {
      return null;
    }
    return Reference.builder()
        .display(maybeReference.display().orElse(null))
        .reference(path.orElse(null))
        .build();
  }
}
