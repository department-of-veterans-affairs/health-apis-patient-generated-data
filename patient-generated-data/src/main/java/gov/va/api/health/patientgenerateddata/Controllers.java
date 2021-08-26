package gov.va.api.health.patientgenerateddata;

import static com.google.common.base.Preconditions.checkState;
import static java.time.temporal.ChronoUnit.MILLIS;

import gov.va.api.health.r4.api.elements.Meta;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.experimental.UtilityClass;

/** Utilities for R4 controllers. */
@UtilityClass
public class Controllers {
  //  /** Wrapper for Preconditions.checkState which throws a BadRequest. */
  //  @SneakyThrows
  //  public static void checkRequestState(boolean condition, @NonNull String message) {
  //    try {
  //      checkState(condition, message);
  //    } catch (IllegalStateException e) {
  //      throw new Exceptions.BadRequest(e.getMessage(), e);
  //    }
  //  }

  /** Wrapper for Preconditions.checkState which throws a BadRequest. */
  public static void checkRequestState(
      boolean condition, String messageTemplate, Object... messageArgs) {
    try {
      checkState(condition, messageTemplate, messageArgs);
    } catch (IllegalStateException e) {
      throw new Exceptions.BadRequest(e.getMessage(), e);
    }
  }

  /** Generate random ID. */
  public static String generateRandomId() {
    return UUID.randomUUID().toString();
  }

  /** Find and parse lastUpdated from Meta object. */
  public static Optional<Instant> lastUpdatedFromMeta(Meta meta) {
    if (meta == null || meta.lastUpdated() == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(ParseUtils.parseDateTime(meta.lastUpdated()));
  }

  /** Publishes lastUpdated in Meta. */
  public static Meta metaWithLastUpdated(Meta meta, Instant lastUpdated) {
    if (meta == null) {
      meta = Meta.builder().build();
    }
    meta.lastUpdated(lastUpdated.toString());
    return meta;
  }

  /** Current Instant truncated to milliseconds. */
  public static Instant nowMillis() {
    return Instant.now().truncatedTo(MILLIS);
  }
}
