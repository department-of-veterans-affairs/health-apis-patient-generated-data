package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.ContactDetail;
import gov.va.api.health.r4.api.datatypes.ContactPoint;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

public class MetadataControllerTest {
  @Test
  void read() {
    Properties properties = new Properties();
    properties.setProperty("group", "foo.bar");
    properties.setProperty("artifact", "pgd");
    properties.setProperty("version", "3.14159");
    properties.setProperty("time", "2005-01-21T07:57:00Z");
    BuildProperties buildProperties = new BuildProperties(properties);
    assertThat(
            new MetadataController(
                    buildProperties,
                    LinkProperties.builder().baseUrl("http://va.gov").r4BasePath("api/r4").build(),
                    MetadataProperties.builder()
                        .endpointToken("http://fake.com/token")
                        .endpointAuthorize("http://fake.com/authorize")
                        .endpointManagement("http://fake.com/manage")
                        .endpointRevocation("http://fake.com/revoke")
                        .build())
                .read())
        .isEqualTo(
            CapabilityStatement.builder()
                .id("patient-generated-data-capability-statement")
                .resourceType("CapabilityStatement")
                .version("1.0.2")
                .name("API Management Platform | Patient Generated Data - R4")
                .title("API Management Platform | Patient Generated Data - R4")
                .status(CapabilityStatement.Status.active)
                .experimental(true)
                .date("2005-01-21T07:57:00Z")
                .publisher("Department of Veterans Affairs")
                .contact(
                    List.of(
                        ContactDetail.builder()
                            .name("API Support")
                            .telecom(
                                List.of(
                                    ContactPoint.builder()
                                        .system(ContactPoint.ContactPointSystem.email)
                                        .value("api@va.gov")
                                        .build()))
                            .build()))
                .description("Read and search support for FHIR resources.")
                .kind(CapabilityStatement.Kind.capability)
                .software(
                    CapabilityStatement.Software.builder()
                        .name("foo.bar:pgd")
                        .version("3.14159")
                        .releaseDate("2005-01-21T07:57:00Z")
                        .build())
                .implementation(
                    CapabilityStatement.Implementation.builder()
                        .description("API Management Platform | Patient Generated Data - R4")
                        .url("http://va.gov/api/r4")
                        .build())
                .fhirVersion("4.0.1")
                .format(List.of("application/json", "application/fhir+json"))
                .rest(
                    List.of(
                        CapabilityStatement.Rest.builder()
                            .mode(CapabilityStatement.RestMode.server)
                            .resource(
                                List.of(
                                    CapabilityStatement.CapabilityResource.builder()
                                        .type("Observation")
                                        .profile("https://www.hl7.org/fhir/r4/observation.html")
                                        .interaction(
                                            List.of(
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .create),
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .read),
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .update)))
                                        .versioning(CapabilityStatement.Versioning.no_version)
                                        .referencePolicy(
                                            List.of(
                                                CapabilityStatement.ReferencePolicy.literal,
                                                CapabilityStatement.ReferencePolicy.local))
                                        .build(),
                                    CapabilityStatement.CapabilityResource.builder()
                                        .type("Patient")
                                        .profile("https://www.hl7.org/fhir/r4/patient.html")
                                        .interaction(
                                            List.of(
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .create),
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .read),
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .update)))
                                        .versioning(CapabilityStatement.Versioning.no_version)
                                        .referencePolicy(
                                            List.of(
                                                CapabilityStatement.ReferencePolicy.literal,
                                                CapabilityStatement.ReferencePolicy.local))
                                        .build(),
                                    CapabilityStatement.CapabilityResource.builder()
                                        .type("Questionnaire")
                                        .profile("https://www.hl7.org/fhir/r4/questionnaire.html")
                                        .interaction(
                                            List.of(
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .create),
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .read),
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .update),
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .search_type)))
                                        .versioning(CapabilityStatement.Versioning.no_version)
                                        .referencePolicy(
                                            List.of(
                                                CapabilityStatement.ReferencePolicy.literal,
                                                CapabilityStatement.ReferencePolicy.local))
                                        .searchParam(
                                            List.of(
                                                CapabilityStatement.SearchParam.builder()
                                                    .name("_id")
                                                    .type(CapabilityStatement.SearchParamType.token)
                                                    .build(),
                                                CapabilityStatement.SearchParam.builder()
                                                    .name("context-type-value")
                                                    .type(
                                                        CapabilityStatement.SearchParamType
                                                            .composite)
                                                    .build()))
                                        .build(),
                                    CapabilityStatement.CapabilityResource.builder()
                                        .type("QuestionnaireResponse")
                                        .profile(
                                            "https://www.hl7.org/fhir/r4/questionnaireresponse.html")
                                        .interaction(
                                            List.of(
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .create),
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .read),
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .update),
                                                resourceInteraction(
                                                    CapabilityStatement.TypeRestfulInteraction
                                                        .search_type)))
                                        .versioning(CapabilityStatement.Versioning.no_version)
                                        .referencePolicy(
                                            List.of(
                                                CapabilityStatement.ReferencePolicy.literal,
                                                CapabilityStatement.ReferencePolicy.local))
                                        .searchParam(
                                            List.of(
                                                CapabilityStatement.SearchParam.builder()
                                                    .name("_id")
                                                    .type(CapabilityStatement.SearchParamType.token)
                                                    .build(),
                                                CapabilityStatement.SearchParam.builder()
                                                    .name("_tag")
                                                    .type(CapabilityStatement.SearchParamType.token)
                                                    .build(),
                                                CapabilityStatement.SearchParam.builder()
                                                    .name("author")
                                                    .type(
                                                        CapabilityStatement.SearchParamType
                                                            .reference)
                                                    .build(),
                                                CapabilityStatement.SearchParam.builder()
                                                    .name("authored")
                                                    .type(CapabilityStatement.SearchParamType.date)
                                                    .build(),
                                                CapabilityStatement.SearchParam.builder()
                                                    .name("questionnaire")
                                                    .type(
                                                        CapabilityStatement.SearchParamType
                                                            .reference)
                                                    .build(),
                                                CapabilityStatement.SearchParam.builder()
                                                    .name("source")
                                                    .type(
                                                        CapabilityStatement.SearchParamType
                                                            .reference)
                                                    .build(),
                                                CapabilityStatement.SearchParam.builder()
                                                    .name("subject")
                                                    .type(
                                                        CapabilityStatement.SearchParamType
                                                            .reference)
                                                    .build()))
                                        .build()))
                            .security(
                                CapabilityStatement.Security.builder()
                                    .cors(true)
                                    .description("http://docs.smarthealthit.org/")
                                    .service(
                                        List.of(
                                            CodeableConcept.builder()
                                                .coding(
                                                    List.of(
                                                        Coding.builder()
                                                            .system(
                                                                "http://terminology.hl7.org/CodeSystem/restful-security-service")
                                                            .code("SMART-on-FHIR")
                                                            .display("SMART-on-FHIR")
                                                            .build()))
                                                .build()))
                                    .extension(
                                        List.of(
                                            Extension.builder()
                                                .url(
                                                    "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris")
                                                .extension(
                                                    List.of(
                                                        Extension.builder()
                                                            .url("token")
                                                            .valueUri("http://fake.com/token")
                                                            .build(),
                                                        Extension.builder()
                                                            .url("authorize")
                                                            .valueUri("http://fake.com/authorize")
                                                            .build(),
                                                        Extension.builder()
                                                            .url("manage")
                                                            .valueUri("http://fake.com/manage")
                                                            .build(),
                                                        Extension.builder()
                                                            .url("revoke")
                                                            .valueUri("http://fake.com/revoke")
                                                            .build()))
                                                .build()))
                                    .build())
                            .build()))
                .build());
  }

  CapabilityStatement.ResourceInteraction resourceInteraction(
      CapabilityStatement.TypeRestfulInteraction type) {
    return CapabilityStatement.ResourceInteraction.builder()
        .code(type)
        .documentation("Implemented per specification. See http://hl7.org/fhir/R4/http.html")
        .build();
  }

  @Test
  void supportedResource() {
    assertThat(
            MetadataController.SupportedResource.builder()
                .type("type")
                .profileUrl("url")
                .searches(Set.of(MetadataController.SearchParam.PATIENT))
                .build()
                .asResource())
        .isEqualTo(
            CapabilityStatement.CapabilityResource.builder()
                .type("type")
                .profile("url")
                .interaction(
                    List.of(
                        CapabilityStatement.ResourceInteraction.builder()
                            .code(CapabilityStatement.TypeRestfulInteraction.create)
                            .documentation(
                                "Implemented per specification. See http://hl7.org/fhir/R4/http.html")
                            .build(),
                        CapabilityStatement.ResourceInteraction.builder()
                            .code(CapabilityStatement.TypeRestfulInteraction.read)
                            .documentation(
                                "Implemented per specification. See http://hl7.org/fhir/R4/http.html")
                            .build(),
                        CapabilityStatement.ResourceInteraction.builder()
                            .code(CapabilityStatement.TypeRestfulInteraction.update)
                            .documentation(
                                "Implemented per specification. See http://hl7.org/fhir/R4/http.html")
                            .build(),
                        CapabilityStatement.ResourceInteraction.builder()
                            .code(CapabilityStatement.TypeRestfulInteraction.search_type)
                            .documentation(
                                "Implemented per specification. See http://hl7.org/fhir/R4/http.html")
                            .build()))
                .versioning(CapabilityStatement.Versioning.no_version)
                .referencePolicy(
                    List.of(
                        CapabilityStatement.ReferencePolicy.literal,
                        CapabilityStatement.ReferencePolicy.local))
                .searchParam(
                    List.of(
                        CapabilityStatement.SearchParam.builder()
                            .name("patient")
                            .type(CapabilityStatement.SearchParamType.reference)
                            .build()))
                .build());
  }
}
