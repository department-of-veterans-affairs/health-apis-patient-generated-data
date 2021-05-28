package gov.va.api.health.patientgenerateddata;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.lighthouse.vulcan.Vulcan.useUrl;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/** Configuration for public URLs. */
@Getter
@Component
public class LinkProperties {
  private final int defaultPageSize;

  private final int maxPageSize;

  private final String r4Url;

  @Builder
  @Autowired
  LinkProperties(
      @Value("${page-size-default}") int defaultPageSize,
      @Value("${page-size-max}") int maxPageSize,
      @Value("${public-url}") String baseUrl,
      @Value("${public-r4-base-path}") String r4BasePath) {
    this.defaultPageSize = defaultPageSize;
    this.maxPageSize = maxPageSize;

    checkState(!"unset".equals(baseUrl), "public-url is unset");
    checkState(!"unset".equals(r4BasePath), "public-r4-base-path is unset");
    String stripUrl = baseUrl.replaceAll("/$", "");
    checkState(isNotBlank(stripUrl), "public-url is blank");
    String stripR4 = r4BasePath.replaceAll("^/", "").replaceAll("/$", "");
    String combined = stripUrl;
    if (!stripR4.isEmpty()) {
      combined += "/" + stripR4;
    }
    r4Url = combined;
  }

  /**
   * Create standard page configuration for use for Vulcan based controllers. This is expecting a
   * resource name, e.g. Questionnaire and sorting, which is defined on the Resource entities.
   */
  public VulcanConfiguration.PagingConfiguration pagingConfiguration(
      String resource, Sort sorting) {
    return VulcanConfiguration.PagingConfiguration.builder()
        .baseUrlStrategy(useUrl(r4ResourceUrl(resource)))
        .pageParameter("page")
        .countParameter("_count")
        .defaultCount(defaultPageSize)
        .maxCount(maxPageSize)
        .sort(sorting)
        .build();
  }

  public String r4ReadUrl(Resource resource) {
    String name = resource.getClass().getSimpleName();
    return r4ResourceUrl(name) + "/" + resource.id();
  }

  public String r4ResourceUrl(String resource) {
    return r4Url + "/" + resource;
  }
}
