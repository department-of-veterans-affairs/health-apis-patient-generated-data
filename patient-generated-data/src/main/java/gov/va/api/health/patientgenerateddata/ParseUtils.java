package gov.va.api.health.patientgenerateddata;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.Instant;
import java.time.OffsetDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ParseUtils {
  // Date parser switchers based on input length
  // YYYY
  private static final int YEAR = 4;
  // YYYY-MM
  private static final int YEAR_MONTH = 7;
  // YYYY-MM-DD
  private static final int YEAR_MONTH_DAY = 10;

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
}
