package gov.va.api.health.patientgenerateddata;

import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Builder
public class UrlPageLinks {

  private final String baseUrl;

  private final String r4BasePath;

  @Autowired
  public UrlPageLinks(
      @Value("${public-url}") String baseUrl, @Value("${public-r4-base-path}") String r4BasePath) {
    this.baseUrl = baseUrl;
    this.r4BasePath = r4BasePath;
  }

  public String r4Url() {
    return baseUrl.endsWith("/") ? baseUrl + r4BasePath : baseUrl + "/" + r4BasePath;
  }
}
