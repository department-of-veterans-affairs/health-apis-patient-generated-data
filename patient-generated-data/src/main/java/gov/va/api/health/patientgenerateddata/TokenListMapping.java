package gov.va.api.health.patientgenerateddata;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.lighthouse.vulcan.mappings.SingleParameterMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

@Value
@Builder
public final class TokenListMapping<EntityT> implements SingleParameterMapping<EntityT> {
  String parameterName;

  String fieldName;

  static String addTerminators(String str) {
    return "|" + str + "|";
  }

  private static Stream<String> codingJoin(Coding tag) {
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

  /**
   * Takes the questionnaire response and creates pairs of systems and codes, delimited by a comma.
   */
  public static String metadataTagJoin(QuestionnaireResponse questionnaireResponse) {
    if (questionnaireResponse == null
        || questionnaireResponse.meta() == null
        || questionnaireResponse.meta().tag() == null) {
      return null;
    }
    return questionnaireResponse.meta().tag().stream()
        .flatMap(tag -> codingJoin(tag))
        .collect(joining(","));
  }

  /**
   * Takes a set of values to read from the database, and returns any values within the database
   * that contain a given part of the set.
   */
  static <E> Specification<E> selectLikeInList(String fieldName, Set<String> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    ArrayList<String> list = new ArrayList<>(values);
    if (list.size() == 1) {
      return (root, criteriaQuery, criteriaBuilder) ->
          criteriaBuilder.like(
              criteriaBuilder.lower(root.get(fieldName)),
              "%" + addTerminators(list.get(0)).toLowerCase(Locale.ENGLISH) + "%");
    }
    return (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicates =
          list.stream()
              .map(
                  val ->
                      criteriaBuilder.like(
                          criteriaBuilder.lower(root.get(fieldName)),
                          "%" + addTerminators(val).toLowerCase(Locale.ENGLISH) + "%"))
              .collect(toList());
      return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
    };
  }

  @Override
  public Specification<EntityT> specificationFor(HttpServletRequest request) {
    var values =
        Stream.of(request.getParameter(parameterName()).split("\\s*,\\s*"))
            .filter(StringUtils::isNotBlank)
            .filter(str -> !str.equals("|"))
            .collect(toSet());
    return selectLikeInList(fieldName(), values);
  }
}
