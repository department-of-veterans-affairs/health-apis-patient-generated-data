package gov.va.api.health.dataquery.service.controller.procedure;

import static gov.va.api.health.dataquery.service.controller.Transformers.asReferenceId;

import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.Procedure;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Intercept all RequestMapping payloads of Type Procedure.class or Bundle.class. Extract ICN(s)
 * from these payloads with the provided function. This will lead to populating the
 * X-VA-INCLUDES-ICN header.
 */
@ControllerAdvice
public class R4ProcedureIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final ResponseBodyAdvice<Object> delegate =
      IncludesIcnMajig.<Procedure, Procedure.Bundle>builder()
          .type(Procedure.class)
          .bundleType(Procedure.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(body -> Stream.ofNullable(asReferenceId(body.subject())))
          .build();
}
