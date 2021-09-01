package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SourcererTest {
  // {"foo":"bar"}
  final String jwtNoCid =
      "Bearer yJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIifQ.UIZchxQD36xuhacrJF9HQ5SIUxH5HBiv9noESAacsxU";

  // {"cid":"404"}
  final String jwtUnknown =
      "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjaWQiOiI0MDQifQ.fqKxnjh-31G7uLZ327VQJ6artZi9Q-823sPFowNytck";

  Sourcerer s = new Sourcerer("{\"foo\":\"bar\"}", "sat");

  @ParameterizedTest
  @ValueSource(
      strings = {"foo", "sat", "Bearer", "Bearer ", "Bearer notjson", jwtNoCid, jwtUnknown})
  void badRequest(String auth) {
    assertThrows(Exceptions.BadRequest.class, () -> s.source(auth));
  }

  @Test
  void jwt() {
    // {"cid":"foo"}
    String jwtKnown =
        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjaWQiOiJmb28ifQ.tNSiheCHL-YXNkigpMIGyayWponm6fWsYD6EhBBWwck";
    assertThat(s.source(jwtKnown)).isEqualTo("https://api.va.gov/services/pgd/bar");
  }

  @Test
  void staticToken() {
    assertThat(s.source("Bearer sat")).isEqualTo("https://api.va.gov/services/pgd/static-access");
  }
}
