package gov.va.api.health.patientgenerateddata.vulcanizer;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.patientgenerateddata.PayloadEntity;
import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.r4.api.resources.Resource;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import gov.va.api.lighthouse.vulcan.VulcanResult.Paging;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;

@Builder
public class VulcanizedBundler<
        EntityT extends PayloadEntity,
        ResourceT extends Resource,
        EntryT extends AbstractEntry<ResourceT>,
        BundleT extends AbstractBundle<EntryT>>
    implements Function<VulcanResult<EntityT>, BundleT> {
  private final Bundling<ResourceT, EntryT, BundleT> bundling;

  private final Function<EntityT, ResourceT> toResource;

  /** Builder helper to infer generic types. */
  public static <
          EntityT extends PayloadEntity,
          ResourceT extends Resource,
          EntryT extends AbstractEntry<ResourceT>,
          BundleT extends AbstractBundle<EntryT>>
      VulcanizedBundlerBuilder<EntityT, ResourceT, EntryT, BundleT> forBundling(
          Class<EntityT> entity, Bundling<ResourceT, EntryT, BundleT> bundling) {
    // Only need entity for sake of inferring generic types, so just null-check it.
    checkState(entity != null);
    return VulcanizedBundler.<EntityT, ResourceT, EntryT, BundleT>builder().bundling(bundling);
  }

  @Override
  public BundleT apply(VulcanResult<EntityT> result) {
    List<ResourceT> payloads = result.entities().map(toResource).collect(toList());
    List<EntryT> entries = payloads.stream().map(this::toEntry).collect(toList());
    BundleT bundle = bundling.newBundle().get();
    bundle.resourceType("Bundle");
    bundle.type(BundleType.searchset);
    bundle.total((int) result.paging().totalRecords());
    bundle.link(toLinks(result.paging()));
    bundle.entry(entries);
    return bundle;
  }

  private EntryT toEntry(ResourceT resource) {
    EntryT entry = bundling.newEntry().get();
    entry.resource(resource);
    entry.fullUrl(bundling.linkProperties().r4().readUrl(resource));
    entry.search(AbstractEntry.Search.builder().mode(AbstractEntry.SearchMode.match).build());
    return entry;
  }

  private Function<String, BundleLink> toLink(BundleLink.LinkRelation relation) {
    return url -> BundleLink.builder().relation(relation).url(url).build();
  }

  List<BundleLink> toLinks(Paging paging) {
    List<BundleLink> links = new ArrayList<>(5);
    paging.firstPageUrl().map(toLink(LinkRelation.first)).ifPresent(links::add);
    paging.previousPageUrl().map(toLink(LinkRelation.prev)).ifPresent(links::add);
    paging.thisPageUrl().map(toLink(LinkRelation.self)).ifPresent(links::add);
    paging.nextPageUrl().map(toLink(LinkRelation.next)).ifPresent(links::add);
    paging.lastPageUrl().map(toLink(LinkRelation.last)).ifPresent(links::add);
    return links.isEmpty() ? null : links;
  }
}
