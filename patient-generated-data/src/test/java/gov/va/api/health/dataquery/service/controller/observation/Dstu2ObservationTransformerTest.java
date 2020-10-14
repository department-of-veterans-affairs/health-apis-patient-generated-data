package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.health.dataquery.service.controller.observation.Dstu2ObservationTransformer.codeableConcept;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartCoding;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.AntibioticComponent;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.BacteriologyComponent;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.CodeableConcept;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.Quantity;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.ReferenceRange;
import gov.va.api.health.dataquery.service.controller.observation.DatamartObservation.VitalsComponent;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.resources.Observation;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class Dstu2ObservationTransformerTest {
  @Test
  void categoryCoding() {
    assertThat(Dstu2ObservationTransformer.categoryCoding(DatamartObservation.Category.exam))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("exam")
                .display("Exam")
                .build());
    assertThat(Dstu2ObservationTransformer.categoryCoding(DatamartObservation.Category.imaging))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("imaging")
                .display("Imaging")
                .build());
    assertThat(Dstu2ObservationTransformer.categoryCoding(DatamartObservation.Category.laboratory))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("laboratory")
                .display("Laboratory")
                .build());
    assertThat(Dstu2ObservationTransformer.categoryCoding(DatamartObservation.Category.procedure))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("procedure")
                .display("Procedure")
                .build());
    assertThat(
            Dstu2ObservationTransformer.categoryCoding(DatamartObservation.Category.social_history))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("social-history")
                .display("Social History")
                .build());
    assertThat(Dstu2ObservationTransformer.categoryCoding(DatamartObservation.Category.survey))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("survey")
                .display("Survey")
                .build());
    assertThat(Dstu2ObservationTransformer.categoryCoding(DatamartObservation.Category.therapy))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("therapy")
                .display("Therapy")
                .build());
    assertThat(Dstu2ObservationTransformer.categoryCoding(DatamartObservation.Category.vital_signs))
        .isEqualTo(
            Coding.builder()
                .system("http://hl7.org/fhir/observation-category")
                .code("vital-signs")
                .display("Vital Signs")
                .build());
  }

  @Test
  void codeableConceptTransformations() {
    assertThat(codeableConcept(null)).isNull();
    assertThat(codeableConcept(Optional.empty())).isNull();
    assertThat(codeableConcept(Optional.of(CodeableConcept.builder().build()))).isNull();
    // use display as text value
    assertThat(
            codeableConcept(
                Optional.of(
                    CodeableConcept.builder()
                        .coding(
                            Optional.of(
                                DatamartCoding.of().code("c").display("d").system("s").build()))
                        .build())))
        .isEqualTo(
            gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                .text("d")
                .coding(List.of(Coding.builder().display("d").code("c").system("s").build()))
                .build());
    // use given text value as text value
    assertThat(
            codeableConcept(
                Optional.of(
                    CodeableConcept.builder()
                        .text("t")
                        .coding(
                            Optional.of(
                                DatamartCoding.of().code("c").display("d").system("s").build()))
                        .build())))
        .isEqualTo(
            gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                .text("t")
                .coding(List.of(Coding.builder().display("d").code("c").system("s").build()))
                .build());
    // check for a null coding, with a non-blank/non-null text
    assertThat(codeableConcept(Optional.of(CodeableConcept.builder().text("t").build())))
        .isEqualTo(
            gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder().text("t").build());
  }

  @Test
  void componentAntibioticComponentTransformation() {
    assertThat(
            Dstu2ObservationTransformer.component(
                AntibioticComponent.builder()
                    .code(
                        Optional.of(
                            DatamartObservation.CodeableConcept.builder().text("a").build()))
                    .build()))
        .isEqualTo(
            Observation.ObservationComponent.builder()
                .code(
                    gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                        .text("a")
                        .build())
                .build());
    assertThat(
            Dstu2ObservationTransformer.component(
                AntibioticComponent.builder()
                    .valueCodeableConcept(
                        Optional.of(DatamartCoding.builder().code(Optional.of("c")).build()))
                    .build()))
        .isEqualTo(
            Observation.ObservationComponent.builder()
                .valueCodeableConcept(
                    gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                        .coding(singletonList(Coding.builder().code("c").build()))
                        .build())
                .build());
    assertThat(
            Dstu2ObservationTransformer.component(
                AntibioticComponent.builder()
                    .code(
                        Optional.of(
                            DatamartObservation.CodeableConcept.builder().text("a").build()))
                    .valueCodeableConcept(
                        Optional.of(DatamartCoding.builder().code(Optional.of("c")).build()))
                    .build()))
        .isEqualTo(
            Observation.ObservationComponent.builder()
                .code(
                    gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                        .text("a")
                        .build())
                .valueCodeableConcept(
                    gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                        .coding(singletonList(Coding.builder().code("c").build()))
                        .build())
                .build());
  }

  @Test
  void componentVitalsComponentTransformation() {
    assertThat(
            Dstu2ObservationTransformer.component(
                VitalsComponent.builder()
                    .valueQuantity(
                        Optional.of(DatamartObservation.Quantity.builder().code("v").build()))
                    .build()))
        .isEqualTo(
            Observation.ObservationComponent.builder()
                .valueQuantity(
                    gov.va.api.health.dstu2.api.datatypes.Quantity.builder().code("v").build())
                .build());
    assertThat(
            Dstu2ObservationTransformer.component(
                VitalsComponent.builder()
                    .code(Optional.of(DatamartCoding.builder().code(Optional.of("c")).build()))
                    .build()))
        .isEqualTo(
            Observation.ObservationComponent.builder()
                .code(
                    gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                        .coding(singletonList(Coding.builder().code("c").build()))
                        .build())
                .build());
    assertThat(
            Dstu2ObservationTransformer.component(
                VitalsComponent.builder()
                    .valueQuantity(
                        Optional.of(DatamartObservation.Quantity.builder().code("v").build()))
                    .code(Optional.of(DatamartCoding.builder().code(Optional.of("c")).build()))
                    .build()))
        .isEqualTo(
            Observation.ObservationComponent.builder()
                .valueQuantity(
                    gov.va.api.health.dstu2.api.datatypes.Quantity.builder().code("v").build())
                .code(
                    gov.va.api.health.dstu2.api.datatypes.CodeableConcept.builder()
                        .coding(singletonList(Coding.builder().code("c").build()))
                        .build())
                .build());
  }

  @Test
  void empty() {
    assertThat(
            Dstu2ObservationTransformer.builder()
                .datamart(DatamartObservation.builder().build())
                .build()
                .toFhir())
        .isEqualTo(Observation.builder().resourceType("Observation").build());
  }

  @Test
  void interpretationDisplay() {
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("<")).isEqualTo("Off scale low");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay(">")).isEqualTo("Off scale high");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("A")).isEqualTo("Abnormal");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("AA"))
        .isEqualTo("Critically abnormal");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("B")).isEqualTo("Better");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("D"))
        .isEqualTo("Significant change down");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("DET")).isEqualTo("Detected");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("H")).isEqualTo("High");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("HH"))
        .isEqualTo("Critically high");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("HU")).isEqualTo("Very high");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("I")).isEqualTo("Intermediate");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("IE"))
        .isEqualTo("Insufficient evidence");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("IND")).isEqualTo("Indeterminate");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("L")).isEqualTo("Low");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("LL")).isEqualTo("Critically low");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("LU")).isEqualTo("Very low");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("MS"))
        .isEqualTo("Moderately susceptible. Indicates for microbiology susceptibilities only.");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("N")).isEqualTo("Normal");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("ND")).isEqualTo("Not Detected");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("NEG")).isEqualTo("Negative");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("NR")).isEqualTo("Non-reactive");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("NS"))
        .isEqualTo("Non-susceptible");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("POS")).isEqualTo("Positive");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("R")).isEqualTo("Resistant");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("RR")).isEqualTo("Reactive");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("S")).isEqualTo("Susceptible");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("SDD"))
        .isEqualTo("Susceptible-dose dependent");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("SYN-R"))
        .isEqualTo("Synergy - resistant");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("SYN-S"))
        .isEqualTo("Synergy - susceptible");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("U"))
        .isEqualTo("Significant change up");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("VS"))
        .isEqualTo("Very susceptible. Indicates for microbiology susceptibilities only.");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("W")).isEqualTo("Worse");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("WR"))
        .isEqualTo("Weakly reactive");
    assertThat(Dstu2ObservationTransformer.interpretationDisplay("RANDOM")).isNull();
  }

  @Test
  void transformerIsNullSafe() {
    assertThat(Dstu2ObservationTransformer.category(null)).isNull();
    assertThat(codeableConcept(java.util.Optional.empty())).isNull();
    assertThat(codeableConcept(Optional.of(CodeableConcept.builder().build()))).isNull();
    assertThat(Dstu2ObservationTransformer.component((VitalsComponent) null)).isNull();
    assertThat(Dstu2ObservationTransformer.component(VitalsComponent.builder().build())).isNull();
    assertThat(Dstu2ObservationTransformer.component((AntibioticComponent) null)).isNull();
    assertThat(Dstu2ObservationTransformer.component(AntibioticComponent.builder().build()))
        .isNull();
    assertThat(Dstu2ObservationTransformer.component((BacteriologyComponent) null)).isNull();
    assertThat(Dstu2ObservationTransformer.component(BacteriologyComponent.builder().build()))
        .isNull();
    assertThat(Dstu2ObservationTransformer.interpretation(null)).isNull();
    assertThat(Dstu2ObservationTransformer.performers(Lists.emptyList())).isNull();
    assertThat(Dstu2ObservationTransformer.quantity(Optional.of(Quantity.builder().build())))
        .isNull();
    assertThat(Dstu2ObservationTransformer.referenceRanges(Optional.empty())).isNull();
    assertThat(
            Dstu2ObservationTransformer.referenceRanges(
                Optional.of(ReferenceRange.builder().build())))
        .isNull();
    assertThat(Dstu2ObservationTransformer.status(null)).isNull();
  }
}
