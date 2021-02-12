package gov.va.api.health.patientgenerateddata;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.Resource;
import lombok.SneakyThrows;

public interface PayloadEntity<R extends Resource> {
  static final ObjectMapper MAPPER = JacksonMapperConfig.createMapper();

  /** Deserialize payload. */
  @SneakyThrows
  default R deserializePayload() {
    try {
      checkState(payload() != null);
      return MAPPER.readValue(payload(), resourceType());
    } catch (Exception e) {
      throw new Exceptions.InvalidPayload(id(), e);
    }
  }

  String id();

  String payload();

  Class<R> resourceType();
}
