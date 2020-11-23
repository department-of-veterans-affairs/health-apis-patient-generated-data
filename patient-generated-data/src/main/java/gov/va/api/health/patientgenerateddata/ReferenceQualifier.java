package gov.va.api.health.patientgenerateddata;

import gov.va.api.health.fhir.api.IsReference;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
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

  private String qualify(String reference) {
    if (StringUtils.isBlank(reference)) {
      return null;
    }
    if (reference.startsWith("http")) {
      return reference;
    }
    if (reference.startsWith("/")) {
      return baseUrl + "/" + r4BasePath + reference;
    }
    return baseUrl + "/" + r4BasePath + "/" + reference;
  }

  /** Converts the reference to a string. */
  @SneakyThrows
  public String serializeAsField(Object shouldBeReference) {
    if (!(shouldBeReference instanceof IsReference)) {
      throw new IllegalArgumentException(
          "Qualified reference writer cannot serialize: " + shouldBeReference);
    }
    IsReference reference = (IsReference) shouldBeReference;
    return qualify(reference.reference());
  }
}
