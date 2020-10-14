package gov.va.api.health.dataquery.tests.dstu2;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.api.health.dstu2.api.resources.Patient;
import gov.va.api.health.sentinel.Environment;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class PatientIT {
  @Delegate ResourceVerifier verifier = ResourceVerifier.dstu2();

  @Test
  public void advanced() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verifyAll(
        test(
            200,
            Patient.Bundle.class,
            "Patient?family={family}&gender={gender}",
            verifier.ids().pii().family(),
            verifier.ids().pii().gender()),
        test(
            200,
            Patient.Bundle.class,
            "Patient?given={given}&gender={gender}",
            verifier.ids().pii().given(),
            verifier.ids().pii().gender()),
        test(
            200,
            Patient.Bundle.class,
            "Patient?name={name}&birthdate={birthdate}",
            verifier.ids().pii().name(),
            verifier.ids().pii().birthdate()),
        test(
            200,
            Patient.Bundle.class,
            "Patient?name={name}&gender={gender}",
            verifier.ids().pii().name(),
            verifier.ids().pii().gender()),
        /*
         * These are tests for the UnsatisfiedServletRequestParameterException mapping to bad
         * request.
         */
        test(400, OperationOutcome.class, "Patient?given={given}", verifier.ids().pii().given()),
        test(400, OperationOutcome.class, "Patient/"));
  }

  @Test
  public void basic() {
    verifier.verifyAll(
        test(200, Patient.class, "Patient/{id}", verifier.ids().patient()),
        test(200, Patient.Bundle.class, "Patient?_id={id}", verifier.ids().patient()));
  }

  /**
   * The CDW database has disabled patient searching by identifier for both PROD/QA. We will test
   * this only in LOCAL mode against the sandbox db.
   */
  @Test
  public void patientIdentifierSearching() {
    assumeEnvironmentIn(Environment.LOCAL);
    verifier.verify(
        test(200, Patient.Bundle.class, "Patient?identifier={id}", verifier.ids().patient()));
  }

  /**
   * In the PROD/QA environments, patient reading is restricted to your unique access-token. Any IDs
   * but your own are revoked with a 403 Forbidden. In environments where this restriction is
   * lifted, the result of an unknown ID should be 404 Not Found.
   */
  @Test
  public void patientMatching() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    int status = (Environment.get() == Environment.LOCAL) ? 404 : 403;
    verifier.verifyAll(
        test(status, OperationOutcome.class, "Patient/{id}", verifier.ids().unknown()),
        test(status, OperationOutcome.class, "Patient?_id={id}", verifier.ids().unknown()));
  }
}
