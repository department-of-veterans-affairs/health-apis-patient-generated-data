package gov.va.api.health.patientgenerateddata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("metadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = false)
public class MetadataProperties {
  private String id;

  private String version;

  private String dstu2Name;

  private String r4Name;

  private String publisher;

  private String statementType;

  private ContactProperties contact;

  private String publicationDate;

  private String description;

  private String softwareName;

  private boolean productionUse;

  // /**
  // * This is specific to DSTU2, STU3, or R4 and not used across versions. It is also specific to
  // the
  // * implementation itself and hard coded.
  // */
  // @Deprecated private String fhirVersion;
  
  private SecurityProperties security;

  private String resourceDocumentation;

  enum StatementType {
    CLINICIAN,
    PATIENT
  }

  @Data
  @Builder
  @Accessors(fluent = false)
  @NoArgsConstructor
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class ContactProperties {
    private String name;

    private String email;
  }

  @Data
  @Builder
  @Accessors(fluent = false)
  @NoArgsConstructor
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class SecurityProperties {
    private String tokenEndpoint;

    private String authorizeEndpoint;

    private String managementEndpoint;

    private String revocationEndpoint;

    private String description;
  }
}
