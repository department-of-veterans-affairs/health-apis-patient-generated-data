package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class OpenApiControllerTest {
  @Test
  void openApi() {
    assertThat(new OpenApiController().openApi()).isEqualTo("{}");
  }
}
