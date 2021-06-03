package gov.va.api.health.patientgenerateddata.observation;

import gov.va.api.health.r4.api.resources.Observation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Samples {

  public static Observation observation() {
    return observation("x");
  }

  public static Observation observation(String id) {
    return Observation.builder().id(id).status(Observation.ObservationStatus.unknown).build();
  }
}
