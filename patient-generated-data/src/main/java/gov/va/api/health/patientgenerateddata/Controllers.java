package gov.va.api.health.patientgenerateddata;

import static com.google.common.base.Preconditions.checkState;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.base.Splitter;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Resource;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Controllers {
  // Date parser switchers based on input length
  // YYYY
  private static final int YEAR = 4;

  // YYYY-MM
  private static final int YEAR_MONTH = 7;

  // YYYY-MM-DD
  private static final int YEAR_MONTH_DAY = 10;

  /** Wrapper for Preconditions.checkState which throws a BadRequest. */
  public static void checkRequestState(
      boolean condition, String messageTemplate, Object... messageArgs) {
    try {
      checkState(condition, messageTemplate, messageArgs);
    } catch (IllegalStateException e) {
      throw new Exceptions.BadRequest(e.getMessage(), e);
    }
  }

  /** Generate random ID. */
  public static String generateRandomId() {
    return UUID.randomUUID().toString();
  }

  /** Find and parse lastUpdated from Meta object. */
  public static Optional<Instant> lastUpdatedFromMeta(Meta meta) {
    return Optional.ofNullable(meta).map(m -> parseDateTime(m.lastUpdated()));
  }

  /** Validate that the request ICN, if present, is the only ICN for the resource. */
  public static <T extends Resource> void matchIcn(
      String requestIcn, T resource, Function<T, Stream<String>> extractIcns) {
    if (requestIcn == null) {
      return;
    }
    Collection<String> otherIcns =
        extractIcns.apply(resource).distinct().filter(i -> !i.equals(requestIcn)).collect(toSet());
    if (!otherIcns.isEmpty()) {
      throw new Exceptions.Forbidden(
          String.format("Token for ICN %s not allowed access to ICN %s", requestIcn, otherIcns));
    }
  }

  /** Publishes lastUpdated and source in Meta. */
  public static Meta metaWithLastUpdatedAndSource(Meta meta, Instant lastUpdated, String source) {
    return Optional.ofNullable(meta)
        .orElse(Meta.builder().build())
        .lastUpdated(lastUpdated.toString())
        .source(source);
  }

  /** Current Instant truncated to milliseconds. */
  public static Instant nowMillis() {
    return Instant.now().truncatedTo(MILLIS);
  }

  /**
   * Parses a FHIR dateTime string into a Java Instant, according to formats defined in
   * https://www.hl7.org/fhir/datatypes.html#dateTime
   */
  public static Instant parseDateTime(String datetime) {
    if (isBlank(datetime)) {
      return null;
    }
    String str = datetime.trim().toUpperCase();
    switch (str.length()) {
      case YEAR:
        return parseDateTimeUtc(String.format("%s-01-01T00:00:00Z", str));
      case YEAR_MONTH:
        return parseDateTimeUtc(String.format("%s-01T00:00:00Z", str));
      case YEAR_MONTH_DAY:
        return parseDateTimeUtc(String.format("%sT00:00:00Z", str));
      default:
        if (str.endsWith("Z")) {
          return parseDateTimeUtc(str);
        } else {
          return parseDateTimeOffset(str);
        }
    }
  }

  /**
   * Attempt to parse a datetime string with a specified offset, according to formats below.
   *
   * <pre>
   * - YYYY-MM-DD'T'HH:MM:SS-HH:MM
   * - YYYY-MM-DD'T'HH:MM:SS+HH:MM
   * </pre>
   */
  private static Instant parseDateTimeOffset(String datetime) {
    if (isBlank(datetime)) {
      return null;
    }
    OffsetDateTime odt = OffsetDateTime.parse(datetime);
    return odt.toInstant();
  }

  /**
   * Attempt to parse a datetime string at UTC Timezone, according to the format below.
   *
   * <pre>
   * - YYYY-MM-DD'T'HH:MM:SS+HH:MM'Z'
   * </pre>
   */
  private static Instant parseDateTimeUtc(String datetime) {
    if (isBlank(datetime)) {
      return null;
    }
    return Instant.parse(datetime);
  }

  /**
   * Extract resource ID from a reference. This is looking for any number of path elements, then a
   * resource type followed by an ID, e.g. `foo/bar/Patient/1234567890V123456`.
   */
  public static String resourceId(Reference ref) {
    if (ref == null) {
      return null;
    }
    return resourceId(ref.reference());
  }

  /**
   * Extract resource ID from a string. This is looking for any number of path elements, then a
   * resource type followed by an ID, e.g. `foo/bar/Patient/1234567890V123456`.
   */
  public static String resourceId(String str) {
    if (isBlank(str)) {
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

  /** Throw Exceptions.Forbidden if sources don't match. */
  public static void validateSource(String id, String authorizationSource, String originalSource) {
    if (!originalSource.equals(authorizationSource)) {
      throw new Exceptions.Forbidden(
          String.format(
              "For resource %s, request source %s is not authorized to update original source %s",
              id, authorizationSource, originalSource));
    }
  }
}
