package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.Controllers.resourceId;
import static gov.va.api.health.patientgenerateddata.Controllers.resourceType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.va.api.health.r4.api.elements.Reference;
import java.time.format.DateTimeParseException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ControllersTest {
  @Test
  void badRequest() {
    Throwable ex =
        assertThrows(
            Exceptions.BadRequest.class, () -> Controllers.checkRequestState(false, "message"));
    assertThat(ex.getMessage()).isEqualTo("message");
    ex =
        assertThrows(
            Exceptions.BadRequest.class,
            () -> Controllers.checkRequestState(false, "%s foo %s", "hello", "goodbye"));
    assertThat(ex.getMessage()).isEqualTo("hello foo goodbye");
  }

  @Test
  void emptyDates() {
    // null or empty
    assertThat(Controllers.parseDateTime(null)).isNull();
    assertThat(Controllers.parseDateTime("")).isNull();
    assertThat(Controllers.parseDateTime(" ")).isNull();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "foo",
        "2013-",
        "2013-06-18T00:00:00",
        "2013-06-18T00:00:00.599",
        "2013-06-18T00:00:00X",
        "2013-06-18T00:00:00/02:00"
      })
  @SneakyThrows
  void invalidDates(String datetime) {
    assertThatExceptionOfType(DateTimeParseException.class)
        .isThrownBy(() -> Controllers.parseDateTime(datetime));
  }

  @Test
  void resourceId_null() {
    assertThat(resourceId(Reference.builder().build())).isNull();
    assertThat(resourceId(Reference.builder().reference("no").build())).isNull();
    assertThat(resourceId(Reference.builder().reference("/no").build())).isNull();
    assertThat(resourceId(Reference.builder().reference("no/").build())).isNull();
    assertThat(resourceId(Reference.builder().reference("no/ ").build())).isNull();
    assertThat(resourceId(Reference.builder().reference("whatever/no/").build())).isNull();
    assertThat(resourceId(Reference.builder().reference("/whatever/no/").build())).isNull();
  }

  @Test
  void resourceId_valid() {
    assertThat(resourceId(Reference.builder().reference("/Patient/123V456").build()))
        .isEqualTo("123V456");
    assertThat(resourceId(Reference.builder().reference("Patient/123V456").build()))
        .isEqualTo("123V456");
  }

  @Test
  void resourceType_null() {
    assertThat(resourceType(Reference.builder().build())).isNull();
    assertThat(resourceType(Reference.builder().reference("no").build())).isNull();
    assertThat(resourceType(Reference.builder().reference("/no").build())).isNull();
    assertThat(resourceType(Reference.builder().reference("no/").build())).isNull();
    assertThat(resourceType(Reference.builder().reference("no/ ").build())).isNull();
    assertThat(resourceType(Reference.builder().reference("whatever/no/").build())).isNull();
    assertThat(resourceType(Reference.builder().reference("/whatever/no/").build())).isNull();
  }

  @Test
  void resourceType_valid() {
    assertThat(resourceType(Reference.builder().reference("/patient/123V456").build()))
        .isEqualTo("patient");
    assertThat(resourceType(Reference.builder().reference("patient/123V456").build()))
        .isEqualTo("patient");
  }

  @Test
  void validDates() {
    // Year
    assertThat(Controllers.parseDateTime("2013").toString()).isEqualTo("2013-01-01T00:00:00Z");
    // Year month
    assertThat(Controllers.parseDateTime("2013-02").toString()).isEqualTo("2013-02-01T00:00:00Z");
    // Year month day
    assertThat(Controllers.parseDateTime("2013-03-04").toString())
        .isEqualTo("2013-03-04T00:00:00Z");
    // UTC time
    assertThat(Controllers.parseDateTime("2013-06-18T00:00:00Z").toString())
        .isEqualTo("2013-06-18T00:00:00Z");
    // UTC with nanoseconds
    assertThat(Controllers.parseDateTime("2013-06-18T00:00:00.599Z").toString())
        .isEqualTo("2013-06-18T00:00:00.599Z");
    // Offsets
    assertThat(Controllers.parseDateTime("2013-06-18T00:00:00+01:00").toString())
        .isEqualTo("2013-06-17T23:00:00Z");
    assertThat(Controllers.parseDateTime("2013-06-18T00:00:00+01").toString())
        .isEqualTo("2013-06-17T23:00:00Z");
    assertThat(Controllers.parseDateTime("2013-06-18T00:00:00-01:00").toString())
        .isEqualTo("2013-06-18T01:00:00Z");
    // Spaces
    assertThat(Controllers.parseDateTime("  2013-06-18T00:00:00-01:00 ").toString())
        .isEqualTo("2013-06-18T01:00:00Z");
  }
}
