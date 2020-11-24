package gov.va.api.health.patientgenerateddata;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PatientGeneratedDataJacksonMapper {

  private final MagicReferenceConfig magicReferences;

  @Autowired
  public PatientGeneratedDataJacksonMapper(MagicReferenceConfig magicReferences) {
    this.magicReferences = magicReferences;
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = JacksonConfig.createMapper();
    return magicReferences.configure(mapper);
  }
}
