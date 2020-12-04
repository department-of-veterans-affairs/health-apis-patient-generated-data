package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.SerializationUtils.deserializedPayload;

import gov.va.api.health.r4.api.resources.Resource;
import lombok.SneakyThrows;

public interface PayloadEntity {
  Resource deserializePayload();

  /** Deserialize payload. */
  @SneakyThrows
  default <T> T deserializePayload(Class<T> clazz) {
    if (payload() == null) {
      throw new Exceptions.InvalidPayload(id(), new NullPointerException());
    }
    return deserializedPayload(id(), payload(), clazz);
  }

  String id();

  String payload();
}
