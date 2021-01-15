package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.TokenListMapping.addTerminators;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TokenListMappingTest {
  private static QuestionnaireResponse _questionnaireResponse(
      String valueSystem, String valueCode) {
    return QuestionnaireResponse.builder()
        .meta(
            Meta.builder()
                .tag(List.of(Coding.builder().code(valueCode).system(valueSystem).build()))
                .build())
        .build();
  }

  @Test
  void metadataValueJoin_Code() {
    String join = TokenListMapping.metadataValueJoin(_questionnaireResponse(null, "123"));
    assertThat(join).contains(addTerminators("|123"));
    assertThat(join).contains(addTerminators("123"));
    assertThat(join).doesNotContain(addTerminators("clinics|123"));
    assertThat(join).doesNotContain(addTerminators("clinics|"));
  }

  @Test
  void metadataValueJoin_System() {
    String join = TokenListMapping.metadataValueJoin(_questionnaireResponse("clinics", null));
    assertThat(join).contains(addTerminators("clinics|"));
    assertThat(join).contains(addTerminators("clinics"));
    assertThat(join).doesNotContain(addTerminators("clinics|123"));
    assertThat(join).doesNotContain(addTerminators("|123"));
  }

  @Test
  void metadataValueJoin_SystemAndCode() {
    String join = TokenListMapping.metadataValueJoin(_questionnaireResponse("clinics", "123"));
    assertThat(join).contains(addTerminators("clinics|123"));
    assertThat(join).contains(addTerminators("clinics|"));
    assertThat(join).contains(addTerminators("|123"));
  }

  @Test
  void metadataValueJoin_falseMatch() {
    String join = TokenListMapping.metadataValueJoin(_questionnaireResponse("clinics", "123"));
    assertThat(join).doesNotContain(addTerminators("clinics|12"));
    assertThat(join).doesNotContain(addTerminators("linics|123"));
  }

  @Test
  void metadataValueJoin_missingSystemAndCode() {
    String join = TokenListMapping.metadataValueJoin(_questionnaireResponse(null, null));
    assertThat(join).isEmpty();
  }
}
