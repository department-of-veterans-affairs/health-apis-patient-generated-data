package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Samples {
  public static QuestionnaireResponse questionnaireResponse() {
    return questionnaireResponse("x");
  }

  public static QuestionnaireResponse questionnaireResponse(String id) {
    return QuestionnaireResponse.builder()
        .id(id)
        .status(QuestionnaireResponse.Status.completed)
        .build();
  }

  public static QuestionnaireResponse questionnaireResponseWithAuthor(String author) {
    return questionnaireResponse().author(Reference.builder().reference(author).build());
  }

  public static QuestionnaireResponse questionnaireResponseWithLastUpdatedAndSource(
      Instant lastUpdated, String source) {
    return questionnaireResponse()
        .meta(Meta.builder().lastUpdated(lastUpdated.toString()).source(source).build());
  }

  public static QuestionnaireResponse questionnaireResponseWithTags(String... tagsArr) {
    checkArgument(tagsArr.length % 2 == 0);
    List<Coding> tags =
        IntStream.range(0, tagsArr.length / 2)
            .map(i -> 2 * i)
            .mapToObj(i -> Coding.builder().system(tagsArr[i]).code(tagsArr[i + 1]).build())
            .collect(toList());
    return questionnaireResponse().meta(Meta.builder().tag(tags).build());
  }
}
