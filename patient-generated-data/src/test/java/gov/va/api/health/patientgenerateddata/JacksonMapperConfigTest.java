package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.r4.api.DataAbsentReason;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Reference;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class JacksonMapperConfigTest {
  private static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  private static Reference reference(String path) {
    return Reference.builder().display("display-value").reference(path).id("id-value").build();
  }

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
        new JacksonMapperConfig(
                new MagicReferenceConfig(
                    LinkProperties.builder()
                        .baseUrl("https://example.com")
                        .r4BasePath("r4")
                        .build()))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);
    FugaziReferenceMajig actual = MAPPER.readValue(serializedJson, FugaziReferenceMajig.class);
    assertThat(actual).isEqualTo(expected);
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
            ._nope(DataAbsentReason.of(DataAbsentReason.Reason.unsupported))
            .alsoNo(reference("https://example.com/r4/Location/1234"))
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
            .nope(reference("https://example.com/r4/Location/1234"))
            ._nope(DataAbsentReason.of(DataAbsentReason.Reason.unsupported))
            .alsoNo(reference("https://example.com/r4/Location/1234"))
            .ref(reference("https://example.com/r4/AllergyIntolerance/1234"))
            .thing(reference(null))
            .thing(reference(null))
            .thing(reference("http://qualified.is.not/touched"))
            .thing(reference("https://example.com/r4/no/slash"))
            .thing(reference("https://example.com/r4/cool/a/slash"))
            .thing(reference("https://example.com/r4/Location"))
            .thing(reference("https://example.com/r4/Location/1234"))
            .thing(reference("https://example.com/r4/Location/1234"))
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
        new JacksonMapperConfig(
                new MagicReferenceConfig(
                    LinkProperties.builder()
                        .baseUrl("https://example.com")
                        .r4BasePath("r4")
                        .build()))
            .objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(input);
    FugaziReferenceMajig actual = MAPPER.readValue(qualifiedJson, FugaziReferenceMajig.class);
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

    JacksonMapperConfigTest.FugaziReferenceMajig inner;

    String whocares;

    Boolean me;
  }
}
