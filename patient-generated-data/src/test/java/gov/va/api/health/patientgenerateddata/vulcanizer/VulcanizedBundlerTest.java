package gov.va.api.health.patientgenerateddata.vulcanizer;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.patientgenerateddata.UrlPageLinks;
import gov.va.api.health.patientgenerateddata.vulcanizer.Foos.FooBundle;
import gov.va.api.health.patientgenerateddata.vulcanizer.Foos.FooEntity;
import gov.va.api.health.patientgenerateddata.vulcanizer.Foos.FooEntry;
import gov.va.api.health.patientgenerateddata.vulcanizer.Foos.FooResource;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import gov.va.api.lighthouse.vulcan.VulcanResult.Paging;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class VulcanizedBundlerTest {
  UrlPageLinks pageLinks =
      UrlPageLinks.builder().baseUrl("http://foo.com").r4BasePath("r4").build();

  private static Paging paging(
      String urlFormat, int first, int prev, int current, int next, int last, int count) {
    return Paging.builder()
        .firstPage(Optional.of(first))
        .firstPageUrl(Optional.of(String.format(urlFormat, first, count)))
        .previousPage(Optional.of(prev))
        .previousPageUrl(Optional.of(String.format(urlFormat, prev, count)))
        .thisPage(Optional.of(current))
        .thisPageUrl(Optional.of(String.format(urlFormat, current, count)))
        .nextPage(Optional.of(next))
        .nextPageUrl(Optional.of(String.format(urlFormat, next, count)))
        .lastPage(Optional.of(last))
        .lastPageUrl(Optional.of(String.format(urlFormat, last, count)))
        .totalRecords(999)
        .totalPages(last)
        .build();
  }

  @Test
  void apply() {
    VulcanResult<FooEntity> result =
        VulcanResult.<FooEntity>builder()
            .paging(paging("http://foo.com/r4/Foo?patient=p1&page=%d&_count=%d", 1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(
                    new FooEntity("1", "{ id: 1 }"),
                    new FooEntity("2", "{ id: 2 }"),
                    new FooEntity("3", "{ id: 3 }")))
            .build();
    var bundle = bundler().apply(result);
    assertThat(bundle.entry().size()).isEqualTo(3);
  }

  VulcanizedBundler<FooEntity, FooResource, FooEntry, FooBundle> bundler() {
    return VulcanizedBundler.forEntity(FooEntity.class)
        .bundling(
            Bundling.newBundle(FooBundle::new)
                .newEntry(FooEntry::new)
                .linkProperties(LinkProperties.builder().urlPageLinks(pageLinks).build())
                .build())
        .build();
  }
}
