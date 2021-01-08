package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.UsageContext;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CompositeMappingTest {
  private static void _assertSearch(String string, String query) {
    StringBuilder sb = new StringBuilder();
    if (!query.startsWith("|")) {
      sb.append("|");
    }
    sb.append(query);
    if (!query.endsWith("|")) {
      sb.append("|");
    }
    assertThat(string).contains(sb.toString());
  }

  @Test
  void useContextValueJoin() {
    Questionnaire x =
        Questionnaire.builder()
            .useContext(
                List.of(
                    UsageContext.builder()
                        .code(Coding.builder().system("uct").code("venue").build())
                        .valueCodeableConcept(
                            CodeableConcept.builder()
                                .coding(
                                    List.of(Coding.builder().system("clinics").code("123").build()))
                                .build())
                        .build()))
            .build();
    String join = CompositeMapping.useContextValueJoin(x);

    // search by context type with system-only is not supported
    //    _assertSearch(join, "uct|$clinics|");
    //    _assertSearch(join, "uct|$clinics|123");
    //    _assertSearch(join, "uct|$|123");
    //    _assertSearch(join, "uct|$123");

    _assertSearch(join, "uct|venue$clinics|");
    _assertSearch(join, "uct|venue$clinics|123");
    _assertSearch(join, "uct|venue$|123");
    _assertSearch(join, "uct|venue$123");

    _assertSearch(join, "|venue$clinics|");
    _assertSearch(join, "|venue$clinics|123");
    _assertSearch(join, "|venue$|123");
    _assertSearch(join, "|venue$123");

    _assertSearch(join, "venue$clinics|");
    _assertSearch(join, "venue$clinics|123");
    _assertSearch(join, "venue$|123");
    _assertSearch(join, "venue$123");
  }

  @Test
  void useContextValueJoin_substring() {
    Questionnaire x =
        Questionnaire.builder()
            .useContext(
                List.of(
                    UsageContext.builder()
                        .code(Coding.builder().system("uct").code("venue").build())
                        .valueCodeableConcept(
                            CodeableConcept.builder()
                                .coding(
                                    List.of(Coding.builder().system("clinics").code("123").build()))
                                .build())
                        .build()))
            .build();
    assertThat(CompositeMapping.useContextValueJoin(x)).doesNotContain("|venue$12|");
  }
}
