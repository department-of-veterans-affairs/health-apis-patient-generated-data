package gov.va.api.health.patientgenerateddata;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import lombok.NonNull;
import lombok.SneakyThrows;

public class SerializationUtils {
  @SneakyThrows
  static <T> T deserializedPayload(
      @NonNull String id, @NonNull String payload, @NonNull Class<T> clazz) {
    try {
      return JacksonConfig.createMapper().readValue(payload, clazz);
    } catch (Exception e) {
      throw new Exceptions.InvalidPayload(id, e);
    }
  }
}
