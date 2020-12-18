package gov.va.api.health.patientgenerateddata.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.Resource;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPut;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.serializePayload;

import java.io.File;
import lombok.SneakyThrows;

public class SyntheticRefresh {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static String baseDir() {
    return System.getProperty("basedir", ".");
  }

  public static void main(String[] args) {
    refresh("observation", Observation.class);
    refresh("patient", Patient.class);
    refresh("questionnaire", Questionnaire.class);
    refresh("questionnaire-response", QuestionnaireResponse.class);
  }

  @SneakyThrows
  private static <T extends Resource> void refresh(String folder, Class<T> clazz) {
    for (File f :
        new File(baseDir() + "/../patient-generated-data-synthetic/src/test/resources/" + folder)
            .listFiles()) {
      T obj = MAPPER.readValue(f, clazz);
      doPut(clazz.getSimpleName() + "/" + obj.id(), serializePayload(obj), "refresh", null);
    }
  }
}
