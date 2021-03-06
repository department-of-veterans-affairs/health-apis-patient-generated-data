package gov.va.api.health.patientgenerateddata;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

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
@AllArgsConstructor(onConstructor_ = @Autowired)
class MetadataController {
  private static final String NAME = "API Management Platform | Patient Generated Data - R4";

  private static final String RESOURCE_DOCUMENTATION =
      "Implemented per specification. See http://hl7.org/fhir/R4/http.html";

  private final BuildProperties buildProperties;

  private final LinkProperties pageLinks;

  private final MetadataProperties metadataProperties;

  private List<ContactDetail> contact() {
    return List.of(
        ContactDetail.builder()
            .name("API Support")
            .telecom(
                List.of(
                    ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.email)
                        .value("api@va.gov")
                        .build()))
            .build());
  }

  private CapabilityStatement.Implementation implementation() {
    return CapabilityStatement.Implementation.builder()
        .description(NAME)
        .url(pageLinks.r4Url())
        .build();
  }

  @GetMapping
  CapabilityStatement read() {
    return CapabilityStatement.builder()
        .resourceType("CapabilityStatement")
        .id("patient-generated-data-capability-statement")
        .version("1.0.2")
        .name(NAME)
        .title(NAME)
        .publisher("Department of Veterans Affairs")
        .status(CapabilityStatement.Status.active)
        .implementation(implementation())
        .experimental(true)
        .contact(contact())
        .date(buildProperties.getTime().toString())
        .description("Read and search support for FHIR resources.")
        .kind(CapabilityStatement.Kind.capability)
        .software(software())
        .fhirVersion("4.0.1")
        .format(List.of("application/json", "application/fhir+json"))
        .rest(rest())
        .build();
  }

  private List<CapabilityStatement.CapabilityResource> resources() {
    return Stream.of(
            SupportedResource.builder()
                .type("Observation")
                .profileUrl("https://www.hl7.org/fhir/r4/observation.html")
                .build(),
            SupportedResource.builder()
                .type("Questionnaire")
                .profileUrl("https://www.hl7.org/fhir/r4/questionnaire.html")
                .searches(Set.of(SearchParam._ID, SearchParam.CONTEXT_TYPE_VALUE))
                .build(),
            SupportedResource.builder()
                .type("QuestionnaireResponse")
                .profileUrl("https://www.hl7.org/fhir/r4/questionnaireresponse.html")
                .searches(
                    Set.of(
                        SearchParam._ID,
                        SearchParam._TAG,
                        SearchParam.AUTHOR,
                        SearchParam.AUTHORED,
                        SearchParam.QUESTIONNAIRE,
                        SearchParam.SOURCE,
                        SearchParam.SUBJECT))
                .build())
        .map(SupportedResource::asResource)
        .collect(toList());
  }

  private List<CapabilityStatement.Rest> rest() {
    return List.of(
        CapabilityStatement.Rest.builder()
            .mode(CapabilityStatement.RestMode.server)
            .resource(resources())
            .security(restSecurity())
            .build());
  }

  private CapabilityStatement.Security restSecurity() {
    return CapabilityStatement.Security.builder()
        .cors(true)
        .description("http://docs.smarthealthit.org/")
        .service(List.of(smartOnFhirCodeableConcept()))
        .extension(
            List.of(
                Extension.builder()
                    .url("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris")
                    .extension(
                        List.of(
                            Extension.builder()
                                .url("token")
                                .valueUri(metadataProperties.getEndpointToken())
                                .build(),
                            Extension.builder()
                                .url("authorize")
                                .valueUri(metadataProperties.getEndpointAuthorize())
                                .build(),
                            Extension.builder()
                                .url("manage")
                                .valueUri(metadataProperties.getEndpointManagement())
                                .build(),
                            Extension.builder()
                                .url("revoke")
                                .valueUri(metadataProperties.getEndpointRevocation())
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

  @Getter
  @AllArgsConstructor
  enum SearchParam {
    _ID("_id", CapabilityStatement.SearchParamType.token),
    _TAG("_tag", CapabilityStatement.SearchParamType.token),
    AUTHOR("author", CapabilityStatement.SearchParamType.reference),
    AUTHORED("authored", CapabilityStatement.SearchParamType.date),
    CONTEXT_TYPE_VALUE("context-type-value", CapabilityStatement.SearchParamType.composite),
    QUESTIONNAIRE("questionnaire", CapabilityStatement.SearchParamType.reference),
    SOURCE("source", CapabilityStatement.SearchParamType.reference),
    SUBJECT("subject", CapabilityStatement.SearchParamType.reference);

    private final String param;

    private final CapabilityStatement.SearchParamType type;
  }

  @Value
  @Builder
  static final class SupportedResource {
    String type;

    String profileUrl;

    Set<SearchParam> searches;

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
      CapabilityStatement.ResourceInteraction creatable =
          CapabilityStatement.ResourceInteraction.builder()
              .code(CapabilityStatement.TypeRestfulInteraction.create)
              .documentation(RESOURCE_DOCUMENTATION)
              .build();
      CapabilityStatement.ResourceInteraction readable =
          CapabilityStatement.ResourceInteraction.builder()
              .code(CapabilityStatement.TypeRestfulInteraction.read)
              .documentation(RESOURCE_DOCUMENTATION)
              .build();
      CapabilityStatement.ResourceInteraction updatable =
          CapabilityStatement.ResourceInteraction.builder()
              .code(CapabilityStatement.TypeRestfulInteraction.update)
              .documentation(RESOURCE_DOCUMENTATION)
              .build();
      if (isEmpty(searches)) {
        return List.of(creatable, readable, updatable);
      }

      CapabilityStatement.ResourceInteraction searchable =
          CapabilityStatement.ResourceInteraction.builder()
              .code(CapabilityStatement.TypeRestfulInteraction.search_type)
              .documentation(RESOURCE_DOCUMENTATION)
              .build();
      return List.of(creatable, readable, updatable, searchable);
    }

    private List<CapabilityStatement.SearchParam> searchParams() {
      if (isEmpty(searches)) {
        return null;
      }
      return searches.stream()
          .sorted((a, b) -> a.param().compareTo(b.param()))
          .map(
              s -> CapabilityStatement.SearchParam.builder().name(s.param()).type(s.type()).build())
          .collect(toList());
    }
  }
}
