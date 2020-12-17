package gov.va.api.health.patientgenerateddata;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

public class KPop {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static String baseDir() {
    return System.getProperty("basedir", ".");
  }

  @SneakyThrows
  public static void main(String[] args) {
    String sentinel = System.getProperty("sentinel", "unset");
    String token = System.getProperty("access-token", "unset");
    System.out.println("sentinel is " + sentinel);
    System.out.println("access-token is " + token);

    // gather files
    // make put call for each one

    // observation
    // patient
    // questionnaire
    // questionnaire-response
  }
}
