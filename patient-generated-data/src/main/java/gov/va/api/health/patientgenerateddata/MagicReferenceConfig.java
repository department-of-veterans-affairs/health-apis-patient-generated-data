package gov.va.api.health.patientgenerateddata;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import gov.va.api.health.fhir.api.IsReference;
import gov.va.api.health.r4.api.elements.Reference;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MagicReferenceConfig {
  private final String baseUrl;

  private final String r4BasePath;

  @Autowired
  public MagicReferenceConfig(
      @Value("${public-url}") String baseUrl, @Value("${public-r4-base-path}") String r4BasePath) {
    this.baseUrl = baseUrl;
    this.r4BasePath = r4BasePath;
  }

  /** Configures and returns the mapper to support magic references. */
  public ObjectMapper configure(ObjectMapper mapper) {
    mapper.registerModule(new MagicReferenceModule());
    return mapper;
  }

  private final class QualifiedReferenceWriter extends BeanPropertyWriter {

    private QualifiedReferenceWriter(BeanPropertyWriter base) {
      super(base);
    }

    private String qualify(String reference) {
      if (StringUtils.isBlank(reference)) {
        return null;
      }
      if (reference.startsWith("http")) {
        return reference;
      }
      if (reference.startsWith("/")) {
        return baseUrl + "/" + r4BasePath + reference;
      }
      return baseUrl + "/" + r4BasePath + "/" + reference;
    }

    @Override
    @SneakyThrows
    public void serializeAsField(
        Object shouldBeReference, JsonGenerator gen, SerializerProvider prov) {
      if (!(shouldBeReference instanceof IsReference)) {
        throw new IllegalArgumentException(
            "Qualified Reference writer cannot serialize: " + shouldBeReference);
      }
      IsReference reference = (IsReference) shouldBeReference;
      String qualifiedReference = qualify(reference.reference());
      if (qualifiedReference != null) {
        gen.writeStringField(getName(), qualifiedReference);
      }
    }
  }

  private final class MagicReferenceModule extends SimpleModule {
    @Override
    public void setupModule(SetupContext context) {
      super.setupModule(context);
      context.addBeanSerializerModifier(
          new BeanSerializerModifier() {
            private void applyReferenceWriter(List<BeanPropertyWriter> beanProperties) {
              for (int i = 0; i < beanProperties.size(); i++) {
                BeanPropertyWriter beanPropertyWriter = beanProperties.get(i);
                if ("reference".equals(beanPropertyWriter.getName())) {
                  beanProperties.set(i, new QualifiedReferenceWriter(beanPropertyWriter));
                }
              }
            }

            @Override
            public List<BeanPropertyWriter> changeProperties(
                SerializationConfig serialConfig,
                BeanDescription beanDesc,
                List<BeanPropertyWriter> beanProperties) {
              if (beanDesc.getBeanClass() == Reference.class) {
                applyReferenceWriter(beanProperties);
              }
              return super.changeProperties(serialConfig, beanDesc, beanProperties);
            }
          });
    }
  }
}