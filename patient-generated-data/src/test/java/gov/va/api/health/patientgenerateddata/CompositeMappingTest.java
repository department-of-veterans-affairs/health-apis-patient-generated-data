package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.MappingUtils.addTerminators;
import static gov.va.api.health.patientgenerateddata.questionnaire.Samples.questionnaireWithUseContext;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Questionnaire;
import org.junit.jupiter.api.Test;

public class CompositeMappingTest {
  @Test
  void useContextValueJoin_missingUcCode() {
    Questionnaire x = questionnaireWithUseContext("uct", null, "clinics", "123");
    assertThat(CompositeMapping.useContextValueJoin(x)).isEmpty();
  }

  @Test
  void useContextValueJoin_missingValueSystemAndCode() {
    Questionnaire x = questionnaireWithUseContext("uct", "venue", null, null);
    assertThat(CompositeMapping.useContextValueJoin(x)).isEmpty();
  }

  @Test
  void useContextValueJoin_substringFalseMatch() {
    String join =
        CompositeMapping.useContextValueJoin(
            questionnaireWithUseContext("uct", "venue", "clinics", "123"));
    assertThat(join).doesNotContain(addTerminators("venue$clinics|12"));
    assertThat(join).doesNotContain(addTerminators("enue$123"));
  }

  @Test
  void useContextValueJoin_ucCode_valueCode() {
    Questionnaire x = questionnaireWithUseContext("uct", "venue", null, "123");
    String join = CompositeMapping.useContextValueJoin(x);
    // value code and system
    assertThat(join).doesNotContain(addTerminators("venue$clinics|123"));
    // any value code
    assertThat(join).doesNotContain(addTerminators("venue$clinics|"));
    // any value system
    assertThat(join).contains(addTerminators("venue$123"));
    // no value system
    assertThat(join).contains(addTerminators("venue$|123"));
  }

  @Test
  void useContextValueJoin_ucCode_valueSystem() {
    Questionnaire x = questionnaireWithUseContext("uct", "venue", "clinics", null);
    String join = CompositeMapping.useContextValueJoin(x);
    // value code and system
    assertThat(join).doesNotContain(addTerminators("venue$clinics|123"));
    // any value code
    assertThat(join).contains(addTerminators("venue$clinics|"));
    // any value system
    assertThat(join).doesNotContain(addTerminators("venue$123"));
    // no value system
    assertThat(join).doesNotContain(addTerminators("venue$|123"));
  }

  @Test
  void useContextValueJoin_ucCode_valueSystem_valueCode() {
    String join =
        CompositeMapping.useContextValueJoin(
            questionnaireWithUseContext("uct", "venue", "clinics", "123"));
    // value code and system
    assertThat(join).contains(addTerminators("venue$clinics|123"));
    // any value code
    assertThat(join).contains(addTerminators("venue$clinics|"));
    // any value system
    assertThat(join).contains(addTerminators("venue$123"));
    // no value system
    assertThat(join).doesNotContain(addTerminators("venue$|123"));
  }
}
