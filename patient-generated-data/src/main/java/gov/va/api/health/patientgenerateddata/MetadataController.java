package gov.va.api.health.patientgenerateddata;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactDetail;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "/r4/metadata",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor = @__({@Autowired}))
class MetadataController {
  // private final ConfigurableBaseUrlPageLinks pageLinks;
  private final MetadataProperties properties;

  private final BuildProperties buildProperties;

  private List<ContactDetail> contact() {
    return List.of(
        ContactDetail.builder()
            .name(properties.getContact().getName())
            .telecom(
                List.of(
                    ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.email)
                        .value(properties.getContact().getEmail())
                        .build()))
            .build());
  }

  private CapabilityStatement.Implementation implementation() {
    // .url(pageLinks.r4Url())
    // baseUrl + "/" + r4BasePath
    return CapabilityStatement.Implementation.builder()
        .description(properties.getR4Name())
        .url("url")
        .build();
  }

  @GetMapping
  CapabilityStatement read() {
    return CapabilityStatement.builder()
        .resourceType("CapabilityStatement")
        .id(properties.getId())
        .version(properties.getVersion())
        .name(properties.getR4Name())
        .title(properties.getR4Name())
        .publisher(properties.getPublisher())
        .status(CapabilityStatement.Status.active)
        .implementation(implementation())
        .experimental(!properties.isProductionUse())
        .contact(contact())
        .date(properties.getPublicationDate())
        .description(properties.getDescription())
        .kind(CapabilityStatement.Kind.capability)
        .software(software())
        .fhirVersion("4.0.1")
        .format(asList("application/json", "application/fhir+json"))
        .rest(rest())
        .build();
  }

  private List<CapabilityStatement.CapabilityResource> resources() {
    return Stream.of(
            support("AllergyIntolerance")
                .profileUrl(
                    "http://hl7.org/fhir/us/core/StructureDefinition/us-core-allergyintolerance")
                .search(Set.of(SearchParam.PATIENT))
                .build(),
            support("Condition")
                .profileUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition")
                .search(
                    Set.of(SearchParam.PATIENT, SearchParam.CATEGORY, SearchParam.CLINICAL_STATUS))
                .build(),
            support("DiagnosticReport")
                .profileUrl(
                    "http://hl7.org/fhir/us/core/StructureDefinition/us-core-diagnosticreport-lab")
                .search(
                    Set.of(
                        SearchParam.ID,
                        SearchParam.PATIENT,
                        SearchParam.CATEGORY,
                        SearchParam.CODE,
                        SearchParam.DATE,
                        SearchParam.STATUS))
                .build(),
            support("Immunization")
                .profileUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-immunization")
                .search(Set.of(SearchParam.PATIENT))
                .build(),
            support("Location")
                .profileUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-location")
                .search(
                    Set.of(
                        SearchParam.ID,
                        SearchParam.NAME,
                        SearchParam.ADDRESS,
                        SearchParam.ADDRESS_CITY,
                        SearchParam.ADDRESS_STATE,
                        SearchParam.ADDRESS_POSTALCODE))
                .build(),
            support("Medication")
                .profileUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-medication")
                .build(),
            support("MedicationRequest")
                .profileUrl(
                    "http://hl7.org/fhir/us/core/StructureDefinition/us-core-medicationrequest")
                .search(Set.of(SearchParam.ID, SearchParam.INTENT, SearchParam.PATIENT))
                .build(),
            support("Observation")
                .profileUrl(
                    "http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab")
                .search(
                    Set.of(
                        SearchParam.ID,
                        SearchParam.PATIENT,
                        SearchParam.CATEGORY,
                        SearchParam.CODE,
                        SearchParam.DATE))
                .build(),
            support("Organization")
                .profileUrl("http://hl7.org/fhir/us/core/StructureDefinition-us-core-organization")
                .search(
                    Set.of(
                        SearchParam.ID,
                        SearchParam.NAME,
                        SearchParam.ADDRESS,
                        SearchParam.ADDRESS_CITY,
                        SearchParam.ADDRESS_STATE,
                        SearchParam.ADDRESS_POSTALCODE))
                .build(),
            support("Patient")
                .profileUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient")
                .search(
                    Set.of(
                        SearchParam.ID,
                        SearchParam.FAMILY,
                        SearchParam.BIRTH_DATE,
                        SearchParam.NAME,
                        SearchParam.GENDER,
                        SearchParam.GIVEN))
                .build(),
            support("Procedure")
                .profileUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-procedure")
                .search(Set.of(SearchParam.ID, SearchParam.PATIENT, SearchParam.DATE))
                .build())
        .map(SupportedResource::asResource)
        .collect(toList());
  }

  private List<CapabilityStatement.Rest> rest() {
    return List.of(
        CapabilityStatement.Rest.builder()
            .mode(CapabilityStatement.RestMode.server)
            .security(restSecurity())
            .resource(resources())
            .build());
  }

  private CapabilityStatement.Security restSecurity() {
    return CapabilityStatement.Security.builder()
        .cors(true)
        .description(properties.getSecurity().getDescription())
        .service(List.of(smartOnFhirCodeableConcept()))
        .extension(
            List.of(
                Extension.builder()
                    .url("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris")
                    .extension(
                        List.of(
                            Extension.builder()
                                .url("token")
                                .valueUri(properties.getSecurity().getTokenEndpoint())
                                .build(),
                            Extension.builder()
                                .url("authorize")
                                .valueUri(properties.getSecurity().getAuthorizeEndpoint())
                                .build(),
                            Extension.builder()
                                .url("manage")
                                .valueUri(properties.getSecurity().getManagementEndpoint())
                                .build(),
                            Extension.builder()
                                .url("revoke")
                                .valueUri(properties.getSecurity().getRevocationEndpoint())
                                .build()))
                    .build()))
        .build();
  }

  private CodeableConcept smartOnFhirCodeableConcept() {
    return CodeableConcept.builder()
        .coding(
            List.of(
                Coding.builder()
                    .system("http://terminology.hl7.org/CodeSystem/restful-security-service")
                    .code("SMART-on-FHIR")
                    .display("SMART-on-FHIR")
                    .build()))
        .build();
  }

  private CapabilityStatement.Software software() {
    return CapabilityStatement.Software.builder()
        .name(buildProperties.getGroup() + ":" + buildProperties.getArtifact())
        .releaseDate(buildProperties.getTime().toString())
        .version(buildProperties.getVersion())
        .build();
  }

  private SupportedResource.SupportedResourceBuilder support(String type) {
    return SupportedResource.builder().properties(properties).type(type);
  }

  @Getter
  @AllArgsConstructor
  enum SearchParam {
    BIRTH_DATE("birthdate", CapabilityStatement.SearchParamType.date),
    CATEGORY("category", CapabilityStatement.SearchParamType.string),
    CLINICAL_STATUS("clinical-status", CapabilityStatement.SearchParamType.token),
    CODE("code", CapabilityStatement.SearchParamType.token),
    DATE("date", CapabilityStatement.SearchParamType.date),
    FAMILY("family", CapabilityStatement.SearchParamType.string),
    GENDER("gender", CapabilityStatement.SearchParamType.token),
    GIVEN("given", CapabilityStatement.SearchParamType.string),
    ID("_id", CapabilityStatement.SearchParamType.string),
    INTENT("intent", CapabilityStatement.SearchParamType.string),
    NAME("name", CapabilityStatement.SearchParamType.string),
    PATIENT("patient", CapabilityStatement.SearchParamType.reference),
    STATUS("status", CapabilityStatement.SearchParamType.token),
    TYPE("type", CapabilityStatement.SearchParamType.token),
    ADDRESS("address", CapabilityStatement.SearchParamType.string),
    ADDRESS_CITY("address-city", CapabilityStatement.SearchParamType.string),
    ADDRESS_STATE("address-state", CapabilityStatement.SearchParamType.string),
    ADDRESS_POSTALCODE("address-postalcode", CapabilityStatement.SearchParamType.string);

    private final String param;

    private final CapabilityStatement.SearchParamType type;
  }

  @Value
  @Builder
  private static class SupportedResource {
    String type;

    String profileUrl;

    @Builder.Default @NonNull Set<SearchParam> search = emptySet();

    MetadataProperties properties;

    CapabilityStatement.CapabilityResource asResource() {
      return CapabilityStatement.CapabilityResource.builder()
          .type(type)
          .profile(profileUrl)
          .interaction(interactions())
          .searchParam(searchParams())
          .versioning(CapabilityStatement.Versioning.no_version)
          .referencePolicy(
              List.of(
                  CapabilityStatement.ReferencePolicy.literal,
                  CapabilityStatement.ReferencePolicy.local))
          .build();
    }

    private List<CapabilityStatement.ResourceInteraction> interactions() {
      CapabilityStatement.ResourceInteraction readable =
          CapabilityStatement.ResourceInteraction.builder()
              .code(CapabilityStatement.TypeRestfulInteraction.read)
              .documentation(properties.getResourceDocumentation())
              .build();
      if (search.isEmpty()) {
        return List.of(readable);
      }

      CapabilityStatement.ResourceInteraction searchable =
          CapabilityStatement.ResourceInteraction.builder()
              .code(CapabilityStatement.TypeRestfulInteraction.search_type)
              .documentation(properties.getResourceDocumentation())
              .build();
      return List.of(searchable, readable);
    }

    private List<CapabilityStatement.SearchParam> searchParams() {
      if (search.isEmpty()) {
        return null;
      }
      return search
          .stream()
          .sorted((a, b) -> a.param().compareTo(b.param()))
          .map(
              s -> CapabilityStatement.SearchParam.builder().name(s.param()).type(s.type()).build())
          .collect(toList());
    }
  }
}
