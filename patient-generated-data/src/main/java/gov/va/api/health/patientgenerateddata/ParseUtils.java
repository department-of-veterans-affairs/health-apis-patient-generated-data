package gov.va.api.health.patientgenerateddata;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class ParseUtils {
  // Date parser switchers based on input length
  // YYYY
  private static final int YEAR = 4;
  // YYYY-MM
  private static final int YEAR_MONTH = 7;
  // YYYY-MM-DD
  private static final int YEAR_MONTH_DAY = 10;
  // YYYY-MM-DD'T'HH:MM:SSZ
  private static final int UTC_Z = 20;
  // YYYY-MM-DD'T'HH:MM:SS.nnn'Z'
  private static final int UTC_Z_NANOS = 24;
  // YYYY-MM-DD'T'HH:MM:SS-HH:MM
  // YYYY-MM-DD'T'HH:MM:SS+HH:MM
  private static final int TIME_ZONE_OFFSET = 25;

  /**
   * Parses a FHIR dateTime string into a Java Instant, according to formats defined in
   * https://www.hl7.org/fhir/datatypes.html#dateTime
   */
  public static Instant parseDateTime(String datetime) {
    if (StringUtils.isBlank(datetime)) {
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
      case UTC_Z:
      case UTC_Z_NANOS:
        return parseDateTimeUtc(str);
      case TIME_ZONE_OFFSET:
        return parseDateTimeOffset(str);
      default:
        throw new DateTimeParseException("Text '" + str + "' could not be parsed.", str, 0);
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
  public static Instant parseDateTimeOffset(String datetime) {
    if (StringUtils.isBlank(datetime)) {
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
  public static Instant parseDateTimeUtc(String datetime) {
    if (StringUtils.isBlank(datetime)) {
      return null;
    }
    return Instant.parse(datetime);
  }
}