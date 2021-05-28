package gov.va.api.health.patientgenerateddata;

import static org.apache.commons.lang3.StringUtils.isBlank;

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
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class provides the Jackson magic necessary to globally apply special logic for References.
 * It will
 *
 * <ul>
 *   <li>Automatically fully qualify reference URLs with a configurable base url and path
 *   <li>Automatically filter out references for resources that are optional, such as Location
 * </ul>
 *
 * The goal of this class is to minimize impact of reference logic through out the application code
 * base. Instead, the above rules are applied universally during serialization. Unfortunately, to
 * accomplish all of this, we have to get deep in the bowel of Jackson.
 *
 * <ul>
 *   <li>To omit fields that are references to optional resources, we will create a property filter
 *       and apply to all objects using a mix-in. The filter will inspect the value of the field to
 *       determine if the value should be omitted.
 *   <li>To omit references in lists, a bean serialization customizer will be attached.
 *   <li>To fully qualify references, a bean property customizer will be attached.
 * </ul>
 */
@Component
@AllArgsConstructor(onConstructor_ = @Autowired)
public class MagicReferenceConfig {
  private final LinkProperties pageLinks;

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
      if (isBlank(reference)) {
        return null;
      }
      if (reference.startsWith("http")) {
        return reference;
      }
      if (reference.startsWith("/")) {
        return pageLinks.r4Url() + reference;
      }
      return pageLinks.r4Url() + "/" + reference;
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
