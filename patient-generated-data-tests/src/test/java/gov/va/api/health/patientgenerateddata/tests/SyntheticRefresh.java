package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doInternalPost;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPut;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.Resource;
import java.io.File;
import lombok.SneakyThrows;

public class SyntheticRefresh {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static String baseDir() {
    return System.getProperty("basedir", ".");
  }

  private static String clientKey() {
    return System.getProperty("client-key");
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
      try {
        doPut(clazz.getSimpleName() + "/" + obj.id(), obj, "refresh", 200);
      } catch (AssertionError e) {
        doInternalPost(clazz.getSimpleName(), obj, "refresh", 201, clientKey());
      }
    }
  }
}
