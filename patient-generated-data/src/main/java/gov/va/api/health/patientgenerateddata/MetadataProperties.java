package gov.va.api.health.patientgenerateddata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuration for conformance statement. */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("metadata")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataProperties {
  private String endpointAuthorize;
  private String endpointManagement;
  private String endpointRevocation;
  private String endpointToken;
}
