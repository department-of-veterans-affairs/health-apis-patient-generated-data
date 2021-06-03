package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.MappingUtils.addTerminators;
import static gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class TokenListMappingTest {

  @Test
  void metadataTagJoin_Code() {
    String join = TokenListMapping.metadataTagJoin(questionnaireResponse(null, "123"));
    assertThat(join).contains(addTerminators("|123"));
    assertThat(join).contains(addTerminators("123"));
    assertThat(join).doesNotContain(addTerminators("clinics|123"));
    assertThat(join).doesNotContain(addTerminators("clinics|"));
  }

  @Test
  void metadataTagJoin_MultipleSystemsAndCodes() {
    String join =
        TokenListMapping.metadataTagJoin(
            questionnaireResponseCsv("clinics", "123", "somethingElse", "456"));
    assertThat(join).contains(addTerminators("clinics|123"));
    assertThat(join).contains(addTerminators("somethingElse|456"));
    assertThat(join).contains(addTerminators("clinics|"));
    assertThat(join).contains(addTerminators("somethingElse|"));
    assertThat(join).contains(addTerminators("123"));
    assertThat(join).contains(addTerminators("456"));
    assertThat(join).doesNotContain(addTerminators("|123"));
    assertThat(join).doesNotContain(addTerminators("|456"));
  }

  @Test
  void metadataTagJoin_Null() {
    QuestionnaireResponse qr = questionnaireResponseNullValues();
    String join = TokenListMapping.metadataTagJoin(qr);
    assertThat(join).isNull();
    qr.meta().tag(Arrays.asList(null, null));
    join = TokenListMapping.metadataTagJoin(qr);
    assertThat(join).isEmpty();
  }

  @Test
  void metadataTagJoin_System() {
    String join = TokenListMapping.metadataTagJoin(questionnaireResponse("clinics", null));
    assertThat(join).contains(addTerminators("clinics|"));
    assertThat(join).doesNotContain(addTerminators("123"));
    assertThat(join).doesNotContain(addTerminators("clinics|123"));
    assertThat(join).doesNotContain(addTerminators("|123"));
  }

  @Test
  void metadataTagJoin_SystemAndCode() {
    String join = TokenListMapping.metadataTagJoin(questionnaireResponse("clinics", "123"));
    assertThat(join).contains(addTerminators("clinics|123"));
    assertThat(join).contains(addTerminators("clinics|"));
    assertThat(join).contains(addTerminators("123"));
    assertThat(join).doesNotContain(addTerminators("|123"));
  }

  @Test
  void metadataTagJoin_falseMatch() {
    String join = TokenListMapping.metadataTagJoin(questionnaireResponse("clinics", "123"));
    assertThat(join).doesNotContain(addTerminators("clinics|12"));
    assertThat(join).doesNotContain(addTerminators("linics|123"));
  }

  @Test
  void metadataTagJoin_missingSystemAndCode() {
    String join = TokenListMapping.metadataTagJoin(questionnaireResponse(null, null));
    assertThat(join).isEmpty();
  }
}
