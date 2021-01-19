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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyntheticRefresh {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static final String CLIENT_KEY = System.getProperty("client-key", "unset");

  private static final String BASE_DIR = System.getProperty("basedir", ".");

  private static String baseDir() {
    return BASE_DIR;
  }

  private static String clientKey() {
    return CLIENT_KEY;
  }

  private static <T extends Resource> void create(T obj, Class<T> clazz) {
    doInternalPost(clazz.getSimpleName(), obj, "create", 201, clientKey());
    doPut(clazz.getSimpleName() + "/" + obj.id(), obj, "refresh", 200);
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
      var response = doPut(clazz.getSimpleName() + "/" + obj.id(), obj, "refresh", null);
      if (response.response().statusCode() == 404) {
        log.warn(String.format("Creating new resource %s", obj.id()));
        create(obj, clazz);
      }
    }
  }
}
