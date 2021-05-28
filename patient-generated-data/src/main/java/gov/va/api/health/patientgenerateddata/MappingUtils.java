package gov.va.api.health.patientgenerateddata;

import static org.springframework.util.CollectionUtils.isEmpty;

import com.google.common.collect.Iterables;
import java.util.Locale;
import java.util.Set;
import javax.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

/** Utility methods used by Vulcan Mappings. */
@UtilityClass
public class MappingUtils {
  static String addTerminators(String str) {
    return "|" + str + "|";
  }

  static <E> Specification<E> selectLikeInList(String fieldName, Set<String> values) {
    if (isEmpty(values)) {
      return null;
    }
    if (values.size() == 1) {
      return (root, criteriaQuery, criteriaBuilder) ->
          criteriaBuilder.like(
              criteriaBuilder.lower(root.get(fieldName)),
              "%"
                  + addTerminators(Iterables.getOnlyElement(values)).toLowerCase(Locale.ENGLISH)
                  + "%");
    }
    return (root, criteriaQuery, criteriaBuilder) -> {
      return criteriaBuilder.or(
          values.stream()
              .map(
                  val ->
                      criteriaBuilder.like(
                          criteriaBuilder.lower(root.get(fieldName)),
                          "%" + addTerminators(val).toLowerCase(Locale.ENGLISH) + "%"))
              .toArray(Predicate[]::new));
    };
  }
}
