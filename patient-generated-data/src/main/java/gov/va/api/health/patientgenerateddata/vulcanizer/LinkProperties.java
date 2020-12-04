package gov.va.api.health.patientgenerateddata.vulcanizer;

import static gov.va.api.lighthouse.vulcan.Vulcan.useUrl;

import gov.va.api.health.patientgenerateddata.UrlPageLinks;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration.PagingConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

@Configuration
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkProperties {
  // TODO load from properties
  private static final int defaultPageSize = 15;

  private static final int maxPageSize = 100;

  @Autowired private UrlPageLinks urlPageLinks;

  /**
   * Create standard page configuration for use for Vulcan based controllers. This is expecting a
   * resource name, e.g. Questionnaire and sorting, which is defined on the Resource entities.
   */
  public PagingConfiguration pagingConfiguration(String resource, Sort sorting) {
    return PagingConfiguration.builder()
        .baseUrlStrategy(useUrl(r4().resourceUrl(resource)))
        .pageParameter("page")
        .countParameter("_count")
        .defaultCount(defaultPageSize)
        .maxCount(maxPageSize)
        .sort(sorting)
        .build();
  }

  public Links<Resource> r4() {
    return new Links<Resource>(urlPageLinks.r4Url());
  }

  public static class Links<ResourceT> {
    @Getter private final String r4Url;

    Links(String r4Url) {
      this.r4Url = r4Url;
    }

    public String readUrl(Resource resource) {
      return readUrl(resource.getClass().getSimpleName(), resource.id());
    }

    public String readUrl(String resource, String id) {
      return resourceUrl(resource) + "/" + id;
    }

    public String resourceUrl(String resource) {
      return r4Url + "/" + resource;
    }
  }
}
