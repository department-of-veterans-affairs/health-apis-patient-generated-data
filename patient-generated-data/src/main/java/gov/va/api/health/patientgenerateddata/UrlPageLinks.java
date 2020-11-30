package gov.va.api.health.patientgenerateddata;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class UrlPageLinks {
  private final String r4Url;

  @Builder
  @Autowired
  UrlPageLinks(
      @Value("${public-url}") String baseUrl, @Value("${public-r4-base-path}") String r4BasePath) {
    checkState(!"unset".equals(baseUrl));
    checkState(!"unset".equals(r4BasePath));
    String stripUrl = baseUrl.replaceAll("/$", "");
    checkState(isNotBlank(stripUrl));
    String stripR4 = r4BasePath.replaceAll("^/", "").replaceAll("/$", "");
    String combined = stripUrl;
    if (!stripR4.isEmpty()) {
      combined += "/" + stripR4;
    }
    r4Url = combined;
  }
}
