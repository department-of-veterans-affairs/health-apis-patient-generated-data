package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClientKeyProtectedEndpointConfigTest {
  @Test
  void disabledClientKeyDisablesFilter() {
    assertThat(
            new ClientKeyProtectedEndpointConfig()
                .clientKeyProtectedEndpointFilter("disabled")
                .isEnabled())
        .isFalse();
  }

  @Test
  void filterIsEnabled() {
    assertThat(
            new ClientKeyProtectedEndpointConfig()
                .clientKeyProtectedEndpointFilter("pteracuda")
                .isEnabled())
        .isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"unset", "", "     "})
  void unsetClientKeyThrowsException(String clientKey) {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                new ClientKeyProtectedEndpointConfig().clientKeyProtectedEndpointFilter(clientKey));
  }
}
