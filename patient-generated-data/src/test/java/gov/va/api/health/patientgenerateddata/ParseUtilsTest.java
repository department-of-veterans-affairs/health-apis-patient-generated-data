package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.format.DateTimeParseException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ParseUtilsTest {
  @Test
  public void emptyDates() {
    // null or empty
    assertThat(ParseUtils.parseDateTime(null)).isNull();
    assertThat(ParseUtils.parseDateTime("")).isNull();
    assertThat(ParseUtils.parseDateTime(" ")).isNull();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "foo",
        "2013-",
        "2013-06-18T00:00:00",
        "2013-06-18T00:00:00.599",
        "2013-06-18T00:00:00X",
        "2013-06-18T00:00:00/02:00",
        "2013-06-18T00:00:00+02"
      })
  @SneakyThrows
  public void invalidDates(String datetime) {
    assertThatExceptionOfType(DateTimeParseException.class)
        .isThrownBy(() -> ParseUtils.parseDateTime(datetime));
  }

  @Test
  public void validDates() {
    // Year
    assertThat(ParseUtils.parseDateTime("2013").toString()).isEqualTo("2013-01-01T00:00:00Z");
    // Year month
    assertThat(ParseUtils.parseDateTime("2013-02").toString()).isEqualTo("2013-02-01T00:00:00Z");
    // Year month day
    assertThat(ParseUtils.parseDateTime("2013-03-04").toString()).isEqualTo("2013-03-04T00:00:00Z");
    // UTC time
    assertThat(ParseUtils.parseDateTime("2013-06-18T00:00:00Z").toString())
        .isEqualTo("2013-06-18T00:00:00Z");
    // UTC with nanoseconds
    assertThat(ParseUtils.parseDateTime("2013-06-18T00:00:00.599Z").toString())
        .isEqualTo("2013-06-18T00:00:00.599Z");
    // Offsets
    assertThat(ParseUtils.parseDateTime("2013-06-18T00:00:00+01:00").toString())
        .isEqualTo("2013-06-17T23:00:00Z");
    assertThat(ParseUtils.parseDateTime("2013-06-18T00:00:00-01:00").toString())
        .isEqualTo("2013-06-18T01:00:00Z");
    // Spaces
    assertThat(ParseUtils.parseDateTime("  2013-06-18T00:00:00-01:00 ").toString())
        .isEqualTo("2013-06-18T01:00:00Z");
  }
}
