package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.validation.api.ExactlyOneOf;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class R4JacksonMapperTest {

  @Test
  @SneakyThrows
  public void preExistingDarsArePreserved() {
    FugaziReferenceMajig input =
        FugaziReferenceMajig.builder()
            .ref(reference("https://example.com/api/Practitioner/1234"))
            .nope(null)
            ._nope(DataAbsentReason.of(DataAbsentReason.Reason.error))
            .build();
    FugaziReferenceMajig expected =
        FugaziReferenceMajig.builder()
            .ref(reference("https://example.com/api/Practitioner/1234"))
            .nope(null)
            ._nope(DataAbsentReason.of(DataAbsentReason.Reason.error))
            .build();
    String serializedJson =
        new PatientGeneratedDataJacksonMapper(new MagicReferenceConfig("https://example.com", "r4"))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);
    FugaziReferenceMajig actual =
        JacksonConfig.createMapper().readValue(serializedJson, FugaziReferenceMajig.class);
    assertThat(actual).isEqualTo(expected);
  }

  private Reference reference(String path) {
    return Reference.builder().display("display-value").reference(path).id("id-value").build();
  }

  @Test
  @SneakyThrows
  public void referencesAreQualified() {
    FugaziReferenceMajig input =
        FugaziReferenceMajig.builder()
            .whocares("noone")
            .me(true)
            .ref(reference("AllergyIntolerance/1234"))
            .nope(reference("https://example.com/r4/Location/1234"))
            .alsoNo(reference("https://example.com/r4/Location/ 1234"))
            .thing(null)
            .thing(reference(null))
            .thing(reference(""))
            .thing(reference("http://qualified.is.not/touched"))
            .thing(reference("no/slash"))
            .thing(reference("/cool/a/slash"))
            .thing(reference("Location"))
            .thing(reference("Location/1234"))
            .thing(reference("https://example.com/r4/Location/1234"))
            .thing(reference("/Organization"))
            .thing(reference("Organization/1234"))
            .thing(reference("https://example.com/r4/Organization/1234"))
            .thing(reference("Practitioner/987"))
            .inner(
                FugaziReferenceMajig.builder()
                    .ref(
                        Reference.builder()
                            .reference("Practitioner/615f31df-f0c7-5100-ac42-7fb952c630d0")
                            .display(null)
                            .build())
                    .build())
            .build();

    FugaziReferenceMajig expected =
        FugaziReferenceMajig.builder()
            .whocares("noone")
            .me(true)
            ._nope(DataAbsentReason.of(DataAbsentReason.Reason.unsupported))
            .ref(reference("https://example.com/r4/AllergyIntolerance/1234"))
            .thing(reference(null))
            .thing(reference(null))
            .thing(reference("http://qualified.is.not/touched"))
            .thing(reference("https://example.com/r4/no/slash"))
            .thing(reference("https://example.com/r4/cool/a/slash"))
            .thing(reference("https://example.com/r4/Location"))
            .thing(reference("https://example.com/r4/Organization"))
            .thing(reference("https://example.com/r4/Organization/1234"))
            .thing(reference("https://example.com/r4/Organization/1234"))
            .thing(reference("https://example.com/r4/Practitioner/987"))
            .inner(
                FugaziReferenceMajig.builder()
                    .ref(
                        Reference.builder()
                            .reference(
                                "https://example.com/r4/Practitioner/615f31df-f0c7-5100-ac42-7fb952c630d0")
                            .build())
                    .build())
            .build();

    String qualifiedJson =
        new PatientGeneratedDataJacksonMapper(new MagicReferenceConfig("https://example.com", "r4"))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);
    FugaziReferenceMajig actual =
        JacksonConfig.createMapper().readValue(qualifiedJson, FugaziReferenceMajig.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  public void requiredReferencesEmitDar() {
    FugaziRequiredReferenceMajig input =
        FugaziRequiredReferenceMajig.builder()
            .required(reference("https://example.com/api/r4/Location/1234"))
            ._required(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
            .build();
    FugaziRequiredReferenceMajig expected =
        FugaziRequiredReferenceMajig.builder()
            .required(null)
            ._required(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
            .build();
    String qualifiedJson =
        new PatientGeneratedDataJacksonMapper(new MagicReferenceConfig("https://example.com", "r4"))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);
    FugaziRequiredReferenceMajig actual =
        JacksonConfig.createMapper().readValue(qualifiedJson, FugaziRequiredReferenceMajig.class);
    assertThat(actual).isEqualTo(expected);
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(
      fieldVisibility = JsonAutoDetect.Visibility.ANY,
      isGetterVisibility = JsonAutoDetect.Visibility.NONE)
  static final class FugaziReferenceMajig {
    Reference ref;
    Reference nope;
    Extension _nope;
    Reference alsoNo;
    @Singular List<Reference> things;
    R4JacksonMapperTest.FugaziReferenceMajig inner;
    String whocares;
    Boolean me;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  @ExactlyOneOf(
      fields = {"required", "_required"},
      message = "Exactly one required field must be specified")
  static final class FugaziRequiredReferenceMajig {
    Reference required;
    Extension _required;
  }
}
