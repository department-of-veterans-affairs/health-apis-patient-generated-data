package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.UsageContext;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CompositeMappingTest {
  private static String _addTerminators(String query) {
    StringBuilder sb = new StringBuilder();
    if (!query.startsWith("|")) {
      sb.append("|");
    }
    sb.append(query);
    if (!query.endsWith("|")) {
      sb.append("|");
    }
    return sb.toString();
  }

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
  void useContextValueJoin_ucCode_valueSystem() {
    Questionnaire x = _questionnaire("uct", "venue", "clinics", null);
    String join = CompositeMapping.useContextValueJoin(x);

    // value code and system
    assertThat(join).doesNotContain(_addTerminators("venue$clinics|123"));

    // any value code
    assertThat(join).contains(_addTerminators("venue$clinics|"));

    // any value system
    assertThat(join).doesNotContain(_addTerminators("venue$123"));

    // no value system
    assertThat(join).doesNotContain(_addTerminators("venue$|123"));
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
  void useContextValueJoin_ucCode_valueCode() {
    Questionnaire x = _questionnaire("uct", "venue", null, "123");
    String join = CompositeMapping.useContextValueJoin(x);

    // value code and system
    assertThat(join).doesNotContain(_addTerminators("venue$clinics|123"));

    // any value code
    assertThat(join).doesNotContain(_addTerminators("venue$clinics|"));

    // any value system
    assertThat(join).contains(_addTerminators("venue$123"));

    // no value system
    assertThat(join).contains(_addTerminators("venue$|123"));
  }

  @Test
  void useContextValueJoin_ucCode_valueSystem_valueCode() {
    String join =
        CompositeMapping.useContextValueJoin(_questionnaire("uct", "venue", "clinics", "123"));

    // value code and system
    assertThat(join).contains(_addTerminators("venue$clinics|123"));

    // any value code
    assertThat(join).contains(_addTerminators("venue$clinics|"));

    // any value system
    assertThat(join).contains(_addTerminators("venue$123"));

    // no value system
    assertThat(join).doesNotContain(_addTerminators("venue$|123"));
  }

  @Test
  void useContextValueJoin_substringFalseMatch() {
    String join =
        CompositeMapping.useContextValueJoin(_questionnaire("uct", "venue", "clinics", "123"));
    assertThat(join).doesNotContain(_addTerminators("venue$clinics|12"));
    assertThat(join).doesNotContain(_addTerminators("enue$123"));
  }

  // search by context type with system-only is not supported
  //    _assertSearch(join, "uct|$clinics|");
  //    _assertSearch(join, "uct|$clinics|123");
  //    _assertSearch(join, "uct|$|123");
  //    _assertSearch(join, "uct|$123");
  //    _assertSearch(join, "uct|venue$clinics|");
  //    _assertSearch(join, "uct|venue$clinics|123");
  //    _assertSearch(join, "uct|venue$|123");
  //    _assertSearch(join, "uct|venue$123");
  //    _assertSearch(join, "|venue$clinics|");
  //    _assertSearch(join, "|venue$clinics|123");
  //    _assertSearch(join, "|venue$|123");
  //    _assertSearch(join, "|venue$123");
}
