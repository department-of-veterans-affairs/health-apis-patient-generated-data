package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Samples {
  public static QuestionnaireResponse questionnaireResponse() {
    return QuestionnaireResponse.builder().status(QuestionnaireResponse.Status.completed).build();
  }

  public static QuestionnaireResponse questionnaireResponse(String id) {
    return QuestionnaireResponse.builder().id(id).build();
  }

  public static QuestionnaireResponse questionnaireResponse(String valueSystem, String valueCode) {
    return QuestionnaireResponse.builder()
        .meta(
            Meta.builder()
                .tag(List.of(Coding.builder().code(valueCode).system(valueSystem).build()))
                .build())
        .build();
  }

  public static QuestionnaireResponse questionnaireResponseCsv(
      String valueSystem, String valueCode, String secondarySystem, String secondaryCode) {
    return QuestionnaireResponse.builder()
        .meta(
            Meta.builder()
                .tag(
                    List.of(
                        Coding.builder().code(valueCode).system(valueSystem).build(),
                        Coding.builder().code(secondaryCode).system(secondarySystem).build()))
                .build())
        .build();
  }

  public static QuestionnaireResponse questionnaireResponseNullValues() {
    return QuestionnaireResponse.builder().meta(Meta.builder().build()).build();
  }

  public static QuestionnaireResponse questionnaireResponseWithAuthor(String author) {
    return QuestionnaireResponse.builder()
        .author(Reference.builder().reference(author).build())
        .build();
  }

  public static QuestionnaireResponse questionnaireResponseWithAuthor(String id, String author) {
    return QuestionnaireResponse.builder()
        .id(id)
        .author(Reference.builder().reference(author).build())
        .build();
  }
}
