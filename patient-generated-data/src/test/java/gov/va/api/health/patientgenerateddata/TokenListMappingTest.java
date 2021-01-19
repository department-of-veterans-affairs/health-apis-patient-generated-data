package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.TokenListMapping.addTerminators;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.util.ArrayList;
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

  private static QuestionnaireResponse _questionnaireResponseNullValues() {
    QuestionnaireResponse qr =
        QuestionnaireResponse.builder()
            .meta(Meta.builder().tag(List.of(Coding.builder().build())).build())
            .build();
    qr.meta().tag(null);
    return qr;
  }

  @Test
  void metadataTagJoin_Code() {
    String join = TokenListMapping.metadataTagJoin(_questionnaireResponse(null, "123"));
    assertThat(join).contains(addTerminators("|123"));
    assertThat(join).contains(addTerminators("123"));
    assertThat(join).doesNotContain(addTerminators("clinics|123"));
    assertThat(join).doesNotContain(addTerminators("clinics|"));
  }

  @Test
  void metadataTagJoin_Null() {
    QuestionnaireResponse qr = _questionnaireResponseNullValues();
    String join = TokenListMapping.metadataTagJoin(qr);
    assertThat(join).isNull();
    List<Coding> list = new ArrayList<>();
    list.add(null);
    qr.meta().tag(list);
    join = TokenListMapping.metadataTagJoin(qr);
    assertThat(join).isEmpty();
  }

  @Test
  void metadataTagJoin_System() {
    String join = TokenListMapping.metadataTagJoin(_questionnaireResponse("clinics", null));
    assertThat(join).contains(addTerminators("clinics|"));
    assertThat(join).contains(addTerminators("clinics"));
    assertThat(join).doesNotContain(addTerminators("clinics|123"));
    assertThat(join).doesNotContain(addTerminators("|123"));
  }

  @Test
  void metadataTagJoin_SystemAndCode() {
    String join = TokenListMapping.metadataTagJoin(_questionnaireResponse("clinics", "123"));
    assertThat(join).contains(addTerminators("clinics|123"));
    assertThat(join).contains(addTerminators("clinics|"));
    assertThat(join).contains(addTerminators("123"));
  }

  @Test
  void metadataTagJoin_falseMatch() {
    String join = TokenListMapping.metadataTagJoin(_questionnaireResponse("clinics", "123"));
    assertThat(join).doesNotContain(addTerminators("clinics|12"));
    assertThat(join).doesNotContain(addTerminators("linics|123"));
  }

  @Test
  void metadataTagJoin_missingSystemAndCode() {
    String join = TokenListMapping.metadataTagJoin(_questionnaireResponse(null, null));
    assertThat(join).isEmpty();
  }
}
