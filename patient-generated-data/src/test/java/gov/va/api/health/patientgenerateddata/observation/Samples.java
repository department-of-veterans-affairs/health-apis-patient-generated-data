package gov.va.api.health.patientgenerateddata.observation;

import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Observation;
import java.time.Instant;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Samples {
  public static Observation observation() {
    return observation("x");
  }

  public static Observation observation(String id) {
    return Observation.builder().id(id).status(Observation.ObservationStatus.unknown).build();
  }

  public static Observation observationWithLastUpdated(Instant lastUpdated) {
    return observation().meta(Meta.builder().lastUpdated(lastUpdated.toString()).build());
  }
}
