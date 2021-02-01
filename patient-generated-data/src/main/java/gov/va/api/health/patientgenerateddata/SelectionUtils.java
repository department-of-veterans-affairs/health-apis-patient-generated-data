package gov.va.api.health.patientgenerateddata;

import static org.springframework.util.CollectionUtils.isEmpty;

import com.google.common.collect.Iterables;
import java.util.Locale;
import java.util.Set;
import javax.persistence.criteria.Predicate;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;

@Value
@Builder
public class SelectionUtils {

  static String addTerminators(String str) {
    return "|" + str + "|";
  }

  /**
   * Takes a set of values to read from the database, and returns any values within the database
   * that contain a given part of the set.
   */
  static <E> Specification<E> selectLikeInList(String fieldName, Set<String> values) {
    if (values == null || isEmpty(values)) {
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
      Predicate[] predicates =
          values.stream()
              .map(
                  val ->
                      criteriaBuilder.like(
                          criteriaBuilder.lower(root.get(fieldName)),
                          "%" + addTerminators(val).toLowerCase(Locale.ENGLISH) + "%"))
              .toArray(Predicate[]::new);
      return criteriaBuilder.or(predicates);
    };
  }
}
