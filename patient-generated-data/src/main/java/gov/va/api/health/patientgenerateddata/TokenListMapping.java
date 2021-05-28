package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.MappingUtils.addTerminators;
import static gov.va.api.health.patientgenerateddata.MappingUtils.selectLikeInList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import com.google.common.base.Splitter;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.lighthouse.vulcan.mappings.SingleParameterMapping;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

/** Vulcan mapping handler for CSV-formatted values. */
@Value
@Builder
public final class TokenListMapping<EntityT> implements SingleParameterMapping<EntityT> {
  String parameterName;

  String fieldName;

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

  /** Return CSV of metadata tag queries. */
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

  @Override
  public Specification<EntityT> specificationFor(HttpServletRequest request) {
    var values =
        Splitter.on(",").trimResults().splitToList(request.getParameter(parameterName())).stream()
            .filter(StringUtils::isNotBlank)
            .filter(str -> !str.equals("|"))
            .collect(toSet());
    return selectLikeInList(fieldName(), values);
  }
}
