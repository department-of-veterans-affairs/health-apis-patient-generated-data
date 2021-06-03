package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Samples {
  public static QuestionnaireResponse questionnaireResponse() {
    return questionnaireResponse(null);
  }

  public static QuestionnaireResponse questionnaireResponse(String id) {
    return QuestionnaireResponse.builder()
        .id(id)
        .status(QuestionnaireResponse.Status.completed)
        .build();
  }

  public static QuestionnaireResponse questionnaireResponseCsv(String... args) {

    if (args.length % 2 != 0) {
      return null;
    }

    List<Coding> tag = new ArrayList<>();

    for (int i = 0; i < args.length; i += 2) {
      var system = args[i];
      var code = args[i + 1];
      tag.add(Coding.builder().code(code).system(system).build());
    }
    return QuestionnaireResponse.builder().meta(Meta.builder().tag(tag).build()).build();
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
