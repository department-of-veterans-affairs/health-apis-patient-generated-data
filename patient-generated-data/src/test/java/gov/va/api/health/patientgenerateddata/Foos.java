package gov.va.api.health.patientgenerateddata;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

class Foos {
  @Builder
  static class FooBundle extends AbstractBundle<FooEntry> {}

  @AllArgsConstructor
  @Data
  static class FooEntity implements PayloadEntity<FooResource> {
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

  static class FooEntry extends AbstractEntry<FooResource> {}

  @Data
  @Builder
  static class FooResource implements Resource {
    String id;

    String implicitRules;

    String language;

    Meta meta;

    String ref;
  }
}
