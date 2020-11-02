package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.resources.Questionnaire;

import java.io.File;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class QuestionnaireExamples {
  @Test
  @SneakyThrows
  public void asdf() {
    //    validate(
    //        "c:/workspace/health-apis-patient-generated-data/patient-generated-data-synthetic"
    //            + "/src/test/resources/questionnaire/clipboard.json");
    //    validate(
    //        "c:/workspace/health-apis-patient-generated-data/patient-generated-data-synthetic"
    //            + "/src/test/resources/questionnaire/1up.json");

    validate(
        "c:/workspace/health-apis-patient-generated-data/patient-generated-data-synthetic"
            + "/src/test/resources/questionnaire/lifelines.json");

    //    validate("c:/temp/questionnaire-examples/questionnaire-cqf-example.json");
    //    validate("c:/temp/questionnaire-examples/questionnaire-example-bluebook.json");
    //    validate("c:/temp/questionnaire-examples/questionnaire-example-f201-lifelines.json");
    //    validate("c:/temp/questionnaire-examples/questionnaire-example.json");
    //
    // validate("c:/temp/questionnaire-examples/questionnaire-zika-virus-exposure-assessment.json");

    // validate("c:/temp/questionnaire-examples/questionnaire-example-gcs.json");

    //    Questionnaire.Bundle x =
    //        JacksonConfig.createMapper()
    //            .readValue(
    //                new File(
    //
    // "c:/temp/questionnaire-examples/questionnaire-profile-example-ussg-fht.json"),
    //                Questionnaire.Bundle.class);
  }

  @SneakyThrows
  private void validate(String name) {
    Questionnaire x = JacksonConfig.createMapper().readValue(new File(name), Questionnaire.class);
    Set<ConstraintViolation<Questionnaire>> violations =
        Validation.buildDefaultValidatorFactory().getValidator().validate(x);
    if (!violations.isEmpty()) {
      System.out.println(violations);
    }
    assertThat(violations).isEmpty();
    JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValue(new File(name), x);
  }
}
