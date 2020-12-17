package gov.va.api.health.patientgenerateddata.tests;

import static com.google.common.base.Preconditions.checkState;
import java.io.File;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Observation;
import lombok.SneakyThrows;

public class SyntheticRefresh {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static String baseDir() {
    return System.getProperty("basedir", ".");
  }

  public static void main(String[] args) {
    String sentinel = System.getProperty("sentinel", "unset");
    String token = System.getProperty("access-token", "unset");
    System.out.println("sentinel is " + sentinel);
    System.out.println("access-token is " + token);

    // gather files
    // make put call for each one

    observation();
    // patient
    // questionnaire
    // questionnaire-response
  }

  @SneakyThrows
  private static void observation() {
    for (File f :
        new File(baseDir() + "../patient-generated-data-synthetic/src/test/resources/observation")
            .listFiles()) {
      Observation observation = MAPPER.readValue(f, Observation.class);
      System.out.println(observation);
    }
  }
}
