package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.CompositeMapping.addTerminators;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.UsageContext;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CompositeMappingTest {
  private static Questionnaire _questionnaire(
      String ucSystem, String ucCode, String valueSystem, String valueCode) {
    return Questionnaire.builder()
        .useContext(
            List.of(
                UsageContext.builder()
                    .code(Coding.builder().system(ucSystem).code(ucCode).build())
                    .valueCodeableConcept(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder().system(valueSystem).code(valueCode).build()))
                            .build())
                    .build()))
        .build();
  }

  @Test
  void useContextValueJoin_missingUcCode() {
    Questionnaire x = _questionnaire("uct", null, "clinics", "123");
    assertThat(CompositeMapping.useContextValueJoin(x)).isEmpty();
  }

  @Test
  void useContextValueJoin_missingValueSystemAndCode() {
    Questionnaire x = _questionnaire("uct", "venue", null, null);
    assertThat(CompositeMapping.useContextValueJoin(x)).isEmpty();
  }

  @Test
  void useContextValueJoin_substringFalseMatch() {
    String join =
        CompositeMapping.useContextValueJoin(_questionnaire("uct", "venue", "clinics", "123"));
    assertThat(join).doesNotContain(addTerminators("venue$clinics|12"));
    assertThat(join).doesNotContain(addTerminators("enue$123"));
  }

  @Test
  void useContextValueJoin_ucCode_valueCode() {
    Questionnaire x = _questionnaire("uct", "venue", null, "123");
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
    Questionnaire x = _questionnaire("uct", "venue", "clinics", null);
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
        CompositeMapping.useContextValueJoin(_questionnaire("uct", "venue", "clinics", "123"));
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
