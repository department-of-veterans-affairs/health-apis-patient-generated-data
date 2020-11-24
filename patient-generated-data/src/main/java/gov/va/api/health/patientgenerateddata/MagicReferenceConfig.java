package gov.va.api.health.patientgenerateddata;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import gov.va.api.health.fhir.api.IsReference;
import gov.va.api.health.r4.api.elements.Reference;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    mapper.addMixIn(Object.class, ApplyOptionalReferenceFilter.class);
    mapper.setFilterProvider(
        new SimpleFilterProvider().addFilter("magic-references", new OptionalReferencesFilter()));
    return mapper;
  }

  @JsonFilter("magic-references")
  private static class ApplyOptionalReferenceFilter {}

  private static final class OptionalReferencesFilter extends SimpleBeanPropertyFilter {

    @Override
    @SneakyThrows
    public void serializeAsField(
        Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) {
      writer.serializeAsField(pojo, jgen, provider);
    }
  }

  @RequiredArgsConstructor
  private static final class OptionalReferenceSerializer<T extends IsReference>
      extends JsonSerializer<T> {
    private final JsonSerializer<T> delegate;

    @Override
    @SneakyThrows
    public void serialize(T value, JsonGenerator jgen, SerializerProvider provider) {
      if (value == null) {
        return;
      }
      delegate.serialize(value, jgen, provider);
    }
  }

  private final class QualifiedReferenceWriter extends BeanPropertyWriter {
    private String basePath;

    private QualifiedReferenceWriter(BeanPropertyWriter base, String basePath) {
      super(base);
      this.basePath = basePath;
    }

    private String qualify(String reference) {
      if (StringUtils.isBlank(reference)) {
        return null;
      }
      if (reference.startsWith("http")) {
        return reference;
      }
      if (reference.startsWith("/")) {
        return baseUrl + "/" + basePath + reference;
      }
      return baseUrl + "/" + basePath + "/" + reference;
    }

    @Override
    @SneakyThrows
    public void serializeAsField(
        Object shouldBeReference, JsonGenerator gen, SerializerProvider prov) {
      if (!(shouldBeReference instanceof IsReference)) {
        throw new IllegalArgumentException(
            "Qualified Reference writer cannot be serialize: " + shouldBeReference);
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
            private void applyReferenceWriter(
                List<BeanPropertyWriter> beanProperties, String basePath) {
              for (int i = 0; i < beanProperties.size(); i++) {
                BeanPropertyWriter beanPropertyWriter = beanProperties.get(i);
                if ("reference".equals(beanPropertyWriter.getName())) {
                  beanProperties.set(i, new QualifiedReferenceWriter(beanPropertyWriter, basePath));
                }
              }
            }

            @Override
            public List<BeanPropertyWriter> changeProperties(
                SerializationConfig serialConfig,
                BeanDescription beanDesc,
                List<BeanPropertyWriter> beanProperties) {
              if (beanDesc.getBeanClass() == Reference.class) {
                applyReferenceWriter(beanProperties, r4BasePath);
              }
              return super.changeProperties(serialConfig, beanDesc, beanProperties);
            }

            @Override
            @SuppressWarnings("unchecked")
            public JsonSerializer<?> modifySerializer(
                SerializationConfig serialConfig,
                BeanDescription beanDesc,
                JsonSerializer<?> serializer) {
              if (IsReference.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new OptionalReferenceSerializer<>((JsonSerializer<IsReference>) serializer);
              }
              return super.modifySerializer(serialConfig, beanDesc, serializer);
            }
          });
    }
  }
}
