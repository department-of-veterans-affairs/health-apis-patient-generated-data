package gov.va.api.health.patientgenerateddata.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Path;

@OpenAPIDefinition(
    security = {
      @SecurityRequirement(
          name = "OauthFlowSandbox",
          scopes = {
            "launch/patient",
            "offline_access",
            "patient/Observation.read",
            "patient/Observation.write",
            "patient/QuestionnaireResponse.read",
            "patient/QuestionnaireResponse.write",
            "system/Questionnaire.read",
            "system/Questionnaire.write"
          }),
      @SecurityRequirement(
          name = "OauthFlowProduction",
          scopes = {
            "launch/patient",
            "offline_access",
            "patient/Observation.read",
            "patient/Observation.write",
            "patient/QuestionnaireResponse.read",
            "patient/QuestionnaireResponse.write",
            "system/Questionnaire.read",
            "system/Questionnaire.write"
          })
    },
    info = @Info(title = "US Core R4", version = "v1"),
    servers = {
      @Server(url = "https://sandbox-api.va.gov/services/pgd/v0/r4/", description = "Sandbox"),
      @Server(url = "https://api.va.gov/services/pgd/v0/r4/", description = "Production")
    },
    externalDocs =
        @ExternalDocumentation(
            description = "US Core Implementation Guide",
            url = "https://build.fhir.org/ig/HL7/US-Core-R4/index.html"))
@SecuritySchemes({
  @SecurityScheme(
      name = "OauthFlowSandbox",
      type = SecuritySchemeType.OAUTH2,
      flows =
          @OAuthFlows(
              authorizationCode =
                  @OAuthFlow(
                      authorizationUrl = "https://sandbox-api.va.gov/oauth2/pgd/v1/authorization",
                      tokenUrl = "https://sandbox-api.va.gov/oauth2/pgd/v1/token",
                      scopes = {
                        @OAuthScope(name = "launch/patient", description = "patient launch"),
                        @OAuthScope(name = "offline_access", description = "offline access"),
                        @OAuthScope(
                            name = "patient/Observation.read",
                            description = "read observations"),
                        @OAuthScope(
                            name = "patient/Observation.write",
                            description = "write observations"),
                        @OAuthScope(
                            name = "patient/QuestionnaireResponse.read",
                            description = "read questionnaire responses"),
                        @OAuthScope(
                            name = "patient/QuestionnaireResponse.write",
                            description = "write questionnaire responses"),
                        @OAuthScope(
                            name = "system/Questionnaire.read",
                            description = "read questionnaires"),
                        @OAuthScope(
                            name = "system/Questionnaire.write",
                            description = "write questionnaires")
                      }))),
  @SecurityScheme(
      name = "OauthFlowProduction",
      type = SecuritySchemeType.OAUTH2,
      flows =
          @OAuthFlows(
              authorizationCode =
                  @OAuthFlow(
                      authorizationUrl = "https://api.va.gov/oauth2/pgd/v1/authorization",
                      tokenUrl = "https://api.va.gov/oauth2/pgd/v1/token",
                      scopes = {
                        @OAuthScope(name = "launch/patient", description = "patient launch"),
                        @OAuthScope(name = "offline_access", description = "offline access"),
                        @OAuthScope(
                            name = "patient/Observation.read",
                            description = "read observations"),
                        @OAuthScope(
                            name = "patient/Observation.write",
                            description = "write observations"),
                        @OAuthScope(
                            name = "patient/QuestionnaireResponse.read",
                            description = "read questionnaire responses"),
                        @OAuthScope(
                            name = "patient/QuestionnaireResponse.write",
                            description = "write questionnaire responses"),
                        @OAuthScope(
                            name = "system/Questionnaire.read",
                            description = "read questionnaires"),
                        @OAuthScope(
                            name = "system/Questionnaire.write",
                            description = "write questionnaires")
                      })))
})
@Path("/")
public interface R4PatientGeneratedDataService
    extends R4ObservationApi, R4QuestionnaireApi, R4QuestionnaireResponseApi {}
