package gov.va.api.health.patientgenerateddata.questionnaire;

import gov.va.api.health.patientgenerateddata.IncludesIcnMajig;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Questionnaire;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all Questionnaire RequestMapping payloads. Extract ICNs with the provided function.
 * These will be used to populate the X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class QuestionnaireIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final ResponseBodyAdvice<Object> delegate =
      IncludesIcnMajig.<Questionnaire, Questionnaire.Bundle>builder()
          .type(Questionnaire.class)
          .bundleType(Questionnaire.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.empty())
          .build();
}
