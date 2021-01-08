package gov.va.api.health.patientgenerateddata;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.UsageContext;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.lighthouse.vulcan.Mapping;
import gov.va.api.lighthouse.vulcan.mappings.SingleParameterMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
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

  @Override
  public Specification<EntityT> specificationFor(HttpServletRequest request) {
    String value = request.getParameter(parameterName());
    if (isBlank(value)) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    if (!value.startsWith("|")) {
      sb.append("|");
    }
    sb.append(value);
    if (!value.endsWith("|")) {
      sb.append("|");
    }

    return (Specification<EntityT>)
        (root, criteriaQuery, criteriaBuilder) ->
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get(fieldName)),
                "%" + sb.toString().toLowerCase(Locale.ENGLISH) + "%");
  }

  public static String useContextValueJoin(Questionnaire questionnaire) {
    if (questionnaire == null || questionnaire.useContext() == null) {
      return null;
    }
    return questionnaire
        .useContext()
        .stream()
        .flatMap(context -> valueJoin(context))
        .collect(joining(","));
  }

  private static Stream<String> valueJoin(UsageContext context) {
    if (context == null
        || context.valueCodeableConcept() == null
        || context.valueCodeableConcept().coding() == null) {
      return Stream.empty();
    }
    return context
        .valueCodeableConcept()
        .coding()
        .stream()
        .flatMap(
            valueCoding -> {
              List<String> codeJoins = codingJoin(context.code(), true);
              List<String> valueJoins = codingJoin(valueCoding, false);
              return codeJoins
                  .stream()
                  .flatMap(
                      cj ->
                          valueJoins
                              .stream()
                              .map(
                                  vj ->
                                      Stream.of(cj, vj)
                                          .filter(StringUtils::isNotBlank)
                                          .collect(joining("$"))));
            })
        .filter(StringUtils::isNotBlank)
        .map(join -> "|" + join + "|");
  }

  private static List<String> codingJoin(Coding coding, boolean systemRequiresCode) {
    if (coding == null) {
      return List.of("");
    }
    String system = trimToNull(coding.system());
    String code = trimToNull(coding.code());
    if (system == null) {
      return code == null ? List.of("") : List.of("|" + code, code);
    }
    if (code == null) {
      return List.of(system + "|");
    }
    return systemRequiresCode
        ? List.of(system + "|" + code)
        : List.of(system + "|" + code, "|" + code, code);
  }
}
