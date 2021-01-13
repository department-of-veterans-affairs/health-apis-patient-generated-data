package gov.va.api.health.patientgenerateddata;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Exceptions {
  public static final class AlreadyExists extends RuntimeException {
    public AlreadyExists(String message) {
      super(message);
    }
  }

  public static final class InvalidPayload extends RuntimeException {
    public InvalidPayload(String id, Throwable cause) {
      super(String.format("Resource %s has invalid payload", id), cause);
    }
  }

  public static final class NotFound extends RuntimeException {
    public NotFound(String message) {
      super(message);
    }
  }

  public static final class BadRequest extends RuntimeException {
    public BadRequest(String message) {
      super(message);
    }

    public BadRequest(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static final class Unauthorized extends RuntimeException {
    public Unauthorized(String message) {
      super(message);
    }
  }
}
