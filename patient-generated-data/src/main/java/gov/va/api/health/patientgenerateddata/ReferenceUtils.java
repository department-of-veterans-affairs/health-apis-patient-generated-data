package gov.va.api.health.patientgenerateddata;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.base.Splitter;
import gov.va.api.health.r4.api.elements.Reference;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReferenceUtils {

  public static String resourceId(Reference ref) {
    return resourceId(ref.reference());
  }

  /**
   * Extract resource ID. This is looking for any number of path elements, then a resource type
   * followed by an ID, e.g. `foo/bar/Patient/1234567890V123456`.
   */
  public static String resourceId(String str) {
    if (str == null || isBlank(str)) {
      return null;
    }
    List<String> splitReference = Splitter.on('/').splitToList(str);
    if (splitReference.size() <= 1) {
      return null;
    }
    if (isBlank(splitReference.get(splitReference.size() - 2))) {
      return null;
    }
    String resourceId = splitReference.get(splitReference.size() - 1);
    if (isBlank(resourceId)) {
      return null;
    }
    return resourceId;
  }

  /**
   * Extract resource type. This is looking for any number of path elements, then a resource type
   * followed by an ID, e.g. `foo/bar/Patient/1234567890V123456`.
   */
  public static String resourceType(Reference ref) {
    if (ref == null || isBlank(ref.reference())) {
      return null;
    }
    List<String> splitReference = Splitter.on('/').splitToList(ref.reference());
    if (splitReference.size() <= 1) {
      return null;
    }
    if (isBlank(splitReference.get(splitReference.size() - 1))) {
      return null;
    }
    String resourceType = splitReference.get(splitReference.size() - 2);
    if (isBlank(resourceType)) {
      return null;
    }
    return resourceType;
  }
}
