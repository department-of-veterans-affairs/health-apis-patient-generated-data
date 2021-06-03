package gov.va.api.health.patientgenerateddata.questionnaire;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.UsageContext;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Samples {

  public static Questionnaire questionnaire() {
    return Questionnaire.builder()
        .title("x")
        .status(Questionnaire.PublicationStatus.active)
        .build();
  }

  public static Questionnaire questionnaire(String id) {
    return Questionnaire.builder()
        .id(id)
        .title("x")
        .status(Questionnaire.PublicationStatus.active)
        .build();
  }

  public static Questionnaire questionnaire(
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
}
