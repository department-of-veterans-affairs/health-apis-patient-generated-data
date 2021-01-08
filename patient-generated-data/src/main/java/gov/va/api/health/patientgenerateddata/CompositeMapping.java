package gov.va.api.health.patientgenerateddata;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import gov.va.api.lighthouse.vulcan.Mapping;
import gov.va.api.lighthouse.vulcan.mappings.SingleParameterMapping;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Value;
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
}
