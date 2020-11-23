package gov.va.api.health.patientgenerateddata;

import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.r4.api.elements.Reference;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReferenceQualifier {
  private final String baseUrl;

  private final String r4BasePath;

  @Autowired
  public ReferenceQualifier(
      @Value("${public-url}") String baseUrl, @Value("${public-r4-base-path}") String r4BasePath) {
    this.baseUrl = baseUrl;
    this.r4BasePath = r4BasePath;
  }

  public static <T> Stream<T> stream(Collection<T> collection) {
    return collection == null ? Stream.empty() : collection.stream();
  }

  private void qualify(Reference wrapper) {
    if (wrapper == null) {
      return;
    }
    String reference = wrapper.reference();
    if (isBlank(reference)) {
      return;
    }
    if (reference.startsWith("http")) {
      return;
    }
    if (reference.startsWith("/")) {
      wrapper.reference(baseUrl + "/" + r4BasePath + reference);
      return;
    }
    wrapper.reference(baseUrl + "/" + r4BasePath + "/" + reference);
  }

  public void qualify(@NonNull Stream<Reference> references) {
    references.forEach(r -> qualify(r));
  }
}
