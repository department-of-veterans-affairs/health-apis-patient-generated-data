package gov.va.api.health.patientgenerateddata;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import gov.va.api.health.r4.api.datatypes.UsageContext;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.lighthouse.vulcan.mappings.SingleParameterMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

/** See https://www.hl7.org/fhir/r4/search.html#composite */
@Value
@Builder
public final class CompositeMapping<EntityT> implements SingleParameterMapping<EntityT> {
  String parameterName;

  String fieldName;

  static String addTerminators(String str) {
    return "|" + str + "|";
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

  /** Return CSV of context-type-value queries. */
  public static String useContextValueJoin(Questionnaire questionnaire) {
    if (questionnaire == null || questionnaire.useContext() == null) {
      return null;
    }
    return questionnaire.useContext().stream()
        .flatMap(context -> valueJoin(context))
        .collect(joining(","));
  }

  private static Stream<String> valueJoin(UsageContext context) {
    if (context == null
        || context.valueCodeableConcept() == null
        || context.valueCodeableConcept().coding() == null) {
      return Stream.empty();
    }
    return context.valueCodeableConcept().coding().stream()
        .flatMap(
            valueCoding -> {
              String ucCode =
                  trimToNull(
                      Optional.ofNullable(context.code()).map(con -> con.code()).orElse(null));
              if (ucCode == null) {
                // must have ucCode to index
                return Stream.empty();
              }
              String valueSystem = trimToNull(valueCoding.system());
              String valueCode = trimToNull(valueCoding.code());
              if (valueSystem == null && valueCode == null) {
                // must have at least one of valueSystem and valueCode to index
                return Stream.empty();
              }
              if (valueSystem == null) {
                // code with any system: valueCode
                // code with no system: |valueCode
                return Stream.of(
                    addTerminators(ucCode + "$" + valueCode),
                    addTerminators(ucCode + "$|" + valueCode));
              }
              if (valueCode == null) {
                // system with any code: valueSystem|
                return Stream.of(addTerminators(ucCode + "$" + valueSystem + "|"));
              }
              // system with explicit code: valueSystem|valueCode
              // system with any code: valueSystem|
              // code with any system: valueCode
              return Stream.of(
                  addTerminators(ucCode + "$" + valueSystem + "|" + valueCode),
                  addTerminators(ucCode + "$" + valueSystem + "|"),
                  addTerminators(ucCode + "$" + valueCode));
            })
        .filter(StringUtils::isNotBlank);
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
