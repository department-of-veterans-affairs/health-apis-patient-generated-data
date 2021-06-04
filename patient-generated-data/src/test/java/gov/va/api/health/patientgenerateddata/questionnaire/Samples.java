package gov.va.api.health.patientgenerateddata.questionnaire;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.UsageContext;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.time.Instant;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Samples {
  public static Questionnaire questionnaire() {
    return questionnaire("x");
  }

  public static Questionnaire questionnaire(String id) {
    return Questionnaire.builder()
        .id(id)
        .title("t")
        .status(Questionnaire.PublicationStatus.active)
        .build();
  }

  public static Questionnaire questionnaireWithLastUpdated(Instant lastUpdated) {
    return questionnaire().meta(Meta.builder().lastUpdated(lastUpdated.toString()).build());
  }

  public static Questionnaire questionnaireWithUseContext(
      String ucSystem, String ucCode, String valueSystem, String valueCode) {
    return questionnaire()
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
                    .build()));
  }
}
