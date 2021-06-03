package gov.va.api.health.patientgenerateddata.observation;

import gov.va.api.health.r4.api.resources.Observation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ObservationSamples {

  public static Observation observation() {
    return observation("x");
  }

  public static Observation observation(String id) {
    return Observation.builder().status(Observation.ObservationStatus.unknown).id(id).build();
  }
}
