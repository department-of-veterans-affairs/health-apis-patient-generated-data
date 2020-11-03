package gov.va.api.health.patientgenerateddata;

import lombok.experimental.UtilityClass;

@UtilityClass
final class Exceptions {
  static final class InvalidPayload extends RuntimeException {
    public InvalidPayload(String id, Throwable cause) {
      super(String.format("Resource %s has invalid payload", id), cause);
    }
  }

  static final class NotFound extends RuntimeException {
    public NotFound(String message) {
      super(message);
    }
  }
}
