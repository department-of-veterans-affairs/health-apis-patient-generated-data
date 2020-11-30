package gov.va.api.health.patientgenerateddata;

import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Builder
public class UrlPageLinks {

  private final String basePath;

  private final String r4BasePath;

  @Autowired
  public UrlPageLinks(
      @Value("${public-url}") String basePath, @Value("${public-r4-base-path}") String r4BasePath) {
    this.basePath = basePath;
    this.r4BasePath = r4BasePath;
  }

  public String r4Url() {
    return basePath + "/" + r4BasePath;
  }
}
