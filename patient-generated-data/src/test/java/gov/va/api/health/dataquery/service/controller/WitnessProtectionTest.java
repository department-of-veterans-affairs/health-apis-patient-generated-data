package gov.va.api.health.dataquery.service.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartReference;
import gov.va.api.health.dataquery.service.controller.datamart.HasReplaceableId;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.util.MultiValueMap;

public class WitnessProtectionTest {
  IdentityService ids = mock(IdentityService.class);
  WitnessProtection wp = new WitnessProtection(ids);

  @Test
  public void registerAndUpdateModifiesReferences() {
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                registration("WITNESS", "wx"),
                registration("WITNESS", "wy"),
                registration("WITNESS", "wz"),
                registration("WHATEVER", "x"),
                registration("WHATEVER", "y"),
                registration("WHATEVER", "z"),
                registration("EVERYONE", "a")));
    Witness x = Witness.of("wxcdw");
    Witness y = Witness.of("wycdw");
    Witness z = Witness.of("wzcdw");
    Map<String, List<DatamartReference>> refs =
        Map.of(
            x.originalId(),
            List.of(
                DatamartReference.of().type("whatever").reference("xcdw").build(),
                DatamartReference.of().type("everyone").reference("acdw").build()),
            y.originalId(),
            List.of(
                DatamartReference.of().type("whatever").reference("ycdw").build(),
                DatamartReference.of().type("everyone").reference("acdw").build()),
            z.originalId(),
            List.of(
                DatamartReference.of().type("whatever").reference("zcdw").build(),
                DatamartReference.of().type("everyone").reference("acdw").build()));
    wp.registerAndUpdateReferences(
        List.of(x, y, z),
        w -> {
          List<DatamartReference> xxx = refs.get(w.originalId());
          return xxx.stream();
        });
    assertThat(x.cdwId()).isEqualTo("wx");
    assertThat(y.cdwId()).isEqualTo("wy");
    assertThat(z.cdwId()).isEqualTo("wz");
    assertThat(refs.get(x.originalId()).get(0).reference().get()).isEqualTo("x");
    assertThat(refs.get(x.originalId()).get(1).reference().get()).isEqualTo("a");
    assertThat(refs.get(y.originalId()).get(0).reference().get()).isEqualTo("y");
    assertThat(refs.get(y.originalId()).get(1).reference().get()).isEqualTo("a");
    assertThat(refs.get(z.originalId()).get(0).reference().get()).isEqualTo("z");
    assertThat(refs.get(z.originalId()).get(1).reference().get()).isEqualTo("a");
  }

  @Test
  public void registerEmptyReturnsEmpty() {
    assertThat(wp.register(null)).isEmpty();
    assertThat(wp.register(List.of())).isEmpty();
  }

  private Registration registration(String resource, String id) {
    return Registration.builder()
        .uuid(id)
        .resourceIdentities(
            List.of(
                ResourceIdentity.builder()
                    .system("CDW")
                    .resource(resource)
                    .identifier(id + "cdw")
                    .build()))
        .build();
  }

  @Test
  public void replacePublicIdsWithCdwIdsReplacesValues() {
    when(ids.lookup("x"))
        .thenReturn(
            List.of(
                ResourceIdentity.builder().system("CDW").resource("X").identifier("XXX").build()));
    MultiValueMap<String, String> actual =
        wp.replacePublicIdsWithCdwIds(Parameters.forIdentity("x"));
    assertThat(actual).isEqualTo(Parameters.forIdentity("XXX"));
  }

  @Test
  public void replacePublicIdsWithCdwIdsThrowsSearchFailedIfIdsFails() {
    when(ids.lookup(Mockito.any())).thenThrow(new IdentityService.LookupFailed("x", "x"));
    assertThrows(
        ResourceExceptions.SearchFailed.class,
        () -> wp.replacePublicIdsWithCdwIds(Parameters.forIdentity("x")));
  }

  @Test
  public void replacePublicIdsWithCdwIdsThrowsUnknownIdentityIfIdsFails() {
    when(ids.lookup(Mockito.any())).thenThrow(new IdentityService.UnknownIdentity("x"));
    assertThrows(
        ResourceExceptions.UnknownIdentityInSearchParameter.class,
        () -> wp.replacePublicIdsWithCdwIds(Parameters.forIdentity("x")));
  }

  @Test
  public void toCdwId() {
    when(ids.lookup("x"))
        .thenReturn(
            List.of(
                ResourceIdentity.builder().system("CDW").resource("X").identifier("XXX").build()));
    assertThat(wp.toCdwId("x")).isEqualTo("XXX");
  }

  @Test
  public void toResourceIdentityExceptionTest() {
    assertThrows(ResourceExceptions.NotFound.class, () -> wp.toResourceIdentity("not cool"));
  }

  @Test
  public void toResourceIdentityTest() {
    ResourceIdentity coolResource =
        ResourceIdentity.builder().system("CDW").resource("COMMUNITY").identifier("ABED").build();
    when(ids.lookup("cool")).thenReturn(List.of(coolResource));
    assertThat(wp.toResourceIdentity("cool")).isEqualTo(coolResource);
  }

  @Data
  @AllArgsConstructor
  private static class Witness implements HasReplaceableId {
    private String objectType;

    private String cdwId;

    private String originalId;

    static Witness of(String id) {
      return new Witness("Witness", id, id);
    }
  }
}
