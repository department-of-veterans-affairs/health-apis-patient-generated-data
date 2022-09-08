package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbusds.jose.JWSObject;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SourcererTest {
  Sourcerer s = new Sourcerer("{\"foo\":\"bar\"}", "sat");

  @ParameterizedTest
  @ValueSource(strings = {"foo", "sat", "Bearer", "Bearer ", "Bearer notjson"})
  void badRequest(String auth) {
    assertThrows(Exceptions.BadRequest.class, () -> s.source(auth));
  }

  @Test
  @SneakyThrows
  void jwt_knownClientId() {
    String jwt =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjaWQiOiJmb28ifQ.tNSiheCHL-YXNkigpMIGyayWponm6fWsYD6EhBBWwck";
    assertThat(JWSObject.parse(jwt).getPayload().toJSONObject()).isEqualTo(Map.of("cid", "foo"));
    assertThat(s.source("Bearer " + jwt)).isEqualTo("https://api.va.gov/services/pgd/bar");
  }

  @Test
  @SneakyThrows
  void jwt_noClientId() {
    String jwt =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIifQ.UIZchxQD36xuhacrJF9HQ5SIUxH5HBiv9noESAacsxU";
    assertThat(JWSObject.parse(jwt).getPayload().toJSONObject()).isEqualTo(Map.of("foo", "bar"));
    assertThrows(Exceptions.BadRequest.class, () -> s.source("Bearer " + jwt));
  }

  @Test
  @SneakyThrows
  void jwt_unknownClientId() {
    String jwt =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjaWQiOiI0MDQifQ.fqKxnjh-31G7uLZ327VQJ6artZi9Q-823sPFowNytck";
    assertThat(JWSObject.parse(jwt).getPayload().toJSONObject()).isEqualTo(Map.of("cid", "404"));
    assertThrows(Exceptions.BadRequest.class, () -> s.source("Bearer " + jwt));
  }

  @Test
  void staticToken() {
    assertThat(s.source("Bearer sat")).isEqualTo("https://api.va.gov/services/pgd/static-access");
  }
}
