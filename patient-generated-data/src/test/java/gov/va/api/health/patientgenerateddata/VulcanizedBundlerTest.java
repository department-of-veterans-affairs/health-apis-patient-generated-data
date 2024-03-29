package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import gov.va.api.lighthouse.vulcan.VulcanResult.Paging;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;

public class VulcanizedBundlerTest {
  LinkProperties pageLinks =
      LinkProperties.builder()
          .defaultPageSize(20)
          .maxPageSize(500)
          .baseUrl("http://foo.com")
          .r4BasePath("r4")
          .build();

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
    return VulcanizedBundler.forBundling(
            FooEntity.class,
            VulcanizedBundler.Bundling.newBundle(FooBundle::new)
                .newEntry(FooEntry::new)
                .linkProperties(pageLinks)
                .build())
        .toResource(FooEntity::deserializePayload)
        .build();
  }

  @Builder
  private static final class FooBundle extends AbstractBundle<FooEntry> {}

  @Data
  @AllArgsConstructor
  private static final class FooEntity implements PayloadEntity<FooResource> {
    String id;

    String payload;

    @Override
    public FooResource deserializePayload() {
      return FooResource.builder().id(id).build();
    }

    public String payload() {
      return payload;
    }

    @Override
    public Class<FooResource> resourceType() {
      return FooResource.class;
    }
  }

  private static final class FooEntry extends AbstractEntry<FooResource> {}

  @Data
  @Builder
  private static final class FooResource implements Resource {
    @Builder.Default String resourceType = "FooResource";

    String id;

    String implicitRules;

    String language;

    Meta meta;

    String ref;
  }
}
