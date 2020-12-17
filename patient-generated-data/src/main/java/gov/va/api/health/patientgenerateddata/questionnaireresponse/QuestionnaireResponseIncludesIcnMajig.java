package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import gov.va.api.health.patientgenerateddata.IncludesIcnMajig;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.util.stream.Stream;
import lombok.experimental.Delegate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import com.google.common.collect.Streams;

@ControllerAdvice
public class QuestionnaireResponseIncludesIcnMajig implements ResponseBodyAdvice<Object> {
  @Delegate
  private final ResponseBodyAdvice<Object> delegate =
      IncludesIcnMajig.<QuestionnaireResponse, QuestionnaireResponse.Bundle>builder()
          .type(QuestionnaireResponse.class)
          .bundleType(QuestionnaireResponse.Bundle.class)
          .extractResources(bundle -> bundle.entry().stream().map(AbstractEntry::resource))
          .extractIcns(
              body ->
                  Streams.concat(
                      Stream.ofNullable(IncludesIcnMajig.icn(body.subject())),
                      Stream.ofNullable(IncludesIcnMajig.icn(body.author())),
                      Stream.ofNullable(IncludesIcnMajig.icn(body.source()))))
          .build();
}
