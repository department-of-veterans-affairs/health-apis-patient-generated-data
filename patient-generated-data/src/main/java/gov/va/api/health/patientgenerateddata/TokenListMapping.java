package gov.va.api.health.patientgenerateddata;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.lighthouse.vulcan.mappings.SingleParameterMapping;
import java.util.Locale;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;

@Value
@Builder
public final class TokenListMapping<EntityT> implements SingleParameterMapping<EntityT> {
  String parameterName;

  String fieldName;

  static String addTerminators(String str) {
    return "|" + str + "|";
  }

  /**
   * Takes the questionnaire response and creates pairs of systems and codes, delimited by a comma.
   */
  public static String metadataValueJoin(QuestionnaireResponse questionnaireResponse) {
    if (questionnaireResponse == null
        || questionnaireResponse.meta() == null
        || questionnaireResponse.meta().tag() == null) {
      return null;
    }
    return questionnaireResponse.meta().tag().stream()
        .flatMap(tag -> valueJoin(tag))
        .collect(joining(" , "));
  }

  private static Stream<String> valueJoin(Coding tag) {
    if (tag == null) {
      return Stream.empty();
    }
    if (tag.system() == null && tag.code() == null) {
      return Stream.empty();
    }
    if (tag.system() == null) {
      return Stream.of(addTerminators("|" + tag.code()), addTerminators(tag.code()));
    }
    if (tag.code() == null) {
      return Stream.of(addTerminators(tag.system() + "|"));
    }
    return Stream.of(
        addTerminators(tag.system() + "|" + tag.code()),
        addTerminators(tag.system() + "|"),
        addTerminators(tag.code()));
  }

  @Override
  public Specification<EntityT> specificationFor(HttpServletRequest request) {
    String value = request.getParameter(parameterName());
    if (isBlank(value)) {
      return null;
    }
    return (Specification<EntityT>)
        (root, criteriaQuery, criteriaBuilder) ->
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                "%" + addTerminators(value).toLowerCase(Locale.ENGLISH) + "%");
  }
}
