package gov.va.api.health.patientgenerateddata;

import com.google.common.base.Preconditions;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Resource;
import lombok.SneakyThrows;

public interface PayloadEntity<R extends Resource> {
  /** Deserialize payload. */
  @SneakyThrows
  default R deserializePayload() {
    try {
      Preconditions.checkState(payload() != null);
      return JacksonConfig.createMapper().readValue(payload(), resourceType());
    } catch (Exception e) {
      throw new Exceptions.InvalidPayload(id(), e);
    }
  }

  String id();

  String payload();

  Class<R> resourceType();
}
