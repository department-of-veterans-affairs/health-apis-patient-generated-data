package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.Requests.ACCESS_TOKEN;
import static gov.va.api.health.patientgenerateddata.tests.Requests.CLIENT_KEY;
import static gov.va.api.health.patientgenerateddata.tests.Requests.MAPPER;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doInternalPost;
import static gov.va.api.health.patientgenerateddata.tests.Requests.doPut;

import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.Resource;
import java.io.File;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyntheticRefresh {
  private static final String BASE_DIR = System.getProperty("basedir", ".");

  public static void main(String[] args) {
    refresh("observation", Observation.class);
    refresh("questionnaire", Questionnaire.class);
    refresh("questionnaire-response", QuestionnaireResponse.class);
  }

  @SneakyThrows
  private static <T extends Resource> void refresh(String folder, Class<T> clazz) {
    for (File f :
        new File(BASE_DIR + "/../patient-generated-data-synthetic/src/test/resources/" + folder)
            .listFiles()) {
      T obj = MAPPER.readValue(f, clazz);
      var response =
          doPut("refresh", clazz.getSimpleName() + "/" + obj.id(), obj, ACCESS_TOKEN, null);
      if (response.response().statusCode() == 404) {
        log.info("Creating {}", clazz.getSimpleName() + "/" + obj.id());
        doInternalPost("create", clazz.getSimpleName(), obj, CLIENT_KEY, 201);
        doPut("refresh", clazz.getSimpleName() + "/" + obj.id(), obj, ACCESS_TOKEN, 200);
      }
    }
  }
}
