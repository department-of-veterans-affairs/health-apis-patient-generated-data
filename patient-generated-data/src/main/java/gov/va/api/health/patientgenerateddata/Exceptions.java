package gov.va.api.health.patientgenerateddata;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Exceptions {
  /** The resource already exists. */
  public static final class AlreadyExists extends RuntimeException {
    public AlreadyExists(String message) {
      super(message);
    }
  }

  /** Payload failed to deserialize. */
  public static final class InvalidPayload extends RuntimeException {
    public InvalidPayload(String id, Throwable cause) {
      super(String.format("Resource %s has invalid payload", id), cause);
    }
  }

  /** The resource is not found. */
  public static final class NotFound extends RuntimeException {
    public NotFound(String message) {
      super(message);
    }
  }

  /** Consumer sent an invalid request. */
  public static final class BadRequest extends RuntimeException {
    public BadRequest(String message) {
      super(message);
    }

    public BadRequest(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /** Get out of here. */
  public static final class Unauthorized extends RuntimeException {
    public Unauthorized(String message) {
      super(message);
    }
  }
}
