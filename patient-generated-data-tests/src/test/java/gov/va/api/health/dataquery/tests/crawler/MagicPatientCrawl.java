package gov.va.api.health.dataquery.tests.crawler;

import static gov.va.api.health.sentinel.SentinelProperties.magicAccessToken;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.dataquery.tests.DataQueryProperties;
import gov.va.api.health.dataquery.tests.SystemDefinition;
import gov.va.api.health.dataquery.tests.SystemDefinitions;
import gov.va.api.health.sentinel.SentinelProperties;
import java.io.File;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class MagicPatientCrawl {

  @Test
  public void crawl() {
    assertThat(magicAccessToken()).isNotNull();
    log.info("Access token is specified");

    String patient = DataQueryProperties.cdwTestPatient();
    log.info("Using patient {} (Override with -Dpatient-id=<id>)", patient);
    Swiggity.swooty(patient);

    SystemDefinition env = SystemDefinitions.systemDefinition();

    ResourceDiscovery discovery =
        ResourceDiscovery.of(
            ResourceDiscovery.Context.builder()
                .patientId(patient)
                .url(CrawlerProperties.baseUrlOrElse(env.dstu2DataQuery().urlWithApiPath()))
                .build());

    IgnoreFilterResultCollector results =
        IgnoreFilterResultCollector.wrap(
            SummarizingResultCollector.wrap(
                new FileResultsCollector(new File("target/patient-crawl-" + patient))));
    results.useFilter(CrawlerProperties.optionCrawlerIgnores());

    RequestQueue rq =
        FilteringRequestQueue.builder()
            .allowQueryUrlPattern(CrawlerProperties.allowQueryUrlPattern())
            .requestQueue(
                UrlReplacementRequestQueue.builder()
                    .replaceUrl(CrawlerProperties.urlReplace())
                    .withUrl(CrawlerProperties.baseUrlOrElse(env.dstu2DataQuery().urlWithApiPath()))
                    .requestQueue(new ConcurrentResourceBalancingRequestQueue())
                    .build())
            .build();

    discovery.queries().forEach(rq::add);
    Crawler crawler =
        Crawler.builder()
            .executor(
                Executors.newFixedThreadPool(
                    SentinelProperties.threadCount("sentinel.crawler.threads", 8)))
            .urlToResourceConverter(UrlToResourceConverter.forFhirVersion(discovery.fhirVersion()))
            .urlExtractor(UrlExtractor.forFhirVersion(discovery.fhirVersion()))
            .requestQueue(rq)
            .results(results)
            .authenticationToken(() -> magicAccessToken())
            .timeLimit(CrawlerProperties.timeLimit())
            .build();
    crawler.crawl();
    log.info("Results for patient : {}", patient);
    assertThat(results.failures()).withFailMessage("%d Failures", results.failures()).isEqualTo(0);
  }
}
