package gov.va.api.health.patientgenerateddata.observation;

import gov.va.api.health.patientgenerateddata.IncludesIcnMajig;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Observation;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of Type Observation.class or Observation.Bundle.class.
 * Extract ICN(s) from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class ObservationIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final ResponseBodyAdvice<Object> delegate =
      IncludesIcnMajig.<Observation, Observation.Bundle>builder()
          .type(Observation.class)
          .bundleType(Observation.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> icns(body))
          .build();

  static Stream<String> icns(Observation observation) {
    return Stream.ofNullable(IncludesIcnMajig.icn(observation.subject()));
  }
}
