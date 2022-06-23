package gov.va.api.health.patientgenerateddata;

import gov.va.api.lighthouse.talos.PathRewriteFilter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Slf4j
@Configuration
public class PathRewriteConfig {
  @Bean
  FilterRegistrationBean<PathRewriteFilter> pathRewriteFilter() {
    var registration = new FilterRegistrationBean<PathRewriteFilter>();
    registration.setOrder(Ordered.LOWEST_PRECEDENCE);
    var filter =
        PathRewriteFilter.builder()
            .removeLeadingPath(List.of("/patient-generated-data/", "/pgd/v0/", "/services/pgd/v0/"))
            .build();
    registration.setFilter(filter);
    registration.addUrlPatterns(filter.removeLeadingPathsAsUrlPatterns());
    log.info("PathRewriteFilter enabled with priority {}", registration.getOrder());
    return registration;
  }
}
