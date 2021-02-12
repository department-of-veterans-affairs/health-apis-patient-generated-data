package gov.va.api.health.patientgenerateddata;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonMapperConfig {
  private final MagicReferenceConfig magicReferences;

  @Autowired
  public JacksonMapperConfig(MagicReferenceConfig magicReferences) {
    this.magicReferences = magicReferences;
  }

  public static ObjectMapper createMapper() {
    return JacksonConfig.createMapper();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return magicReferences.configure(createMapper());
  }
}
