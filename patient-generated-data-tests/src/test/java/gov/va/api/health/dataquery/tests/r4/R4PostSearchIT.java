package gov.va.api.health.dataquery.tests.r4;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentNotIn;

import gov.va.api.health.dataquery.tests.ResourceVerifier;
import gov.va.api.health.r4.api.resources.AllergyIntolerance;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.sentinel.Environment;
import java.util.Map;
import lombok.experimental.Delegate;
import org.junit.jupiter.api.Test;

public class R4PostSearchIT {

  @Delegate ResourceVerifier verifier = ResourceVerifier.r4();

  @Test
  public void basic() {
    assumeEnvironmentNotIn(Environment.LOCAL);
    verifier.verifyAll(
        // Basic Search by Patient
        test(
            200,
            AllergyIntolerance.Bundle.class,
            "AllergyIntolerance/_search",
            Map.of("Content-Type", "application/x-www-form-urlencoded"),
            "patient={patient}",
            verifier.ids().patient()),
        // Parameters mixed in query and body
        test(
            200,
            Condition.Bundle.class,
            "Condition/_search?clinicalstatus=active,resolved",
            Map.of("Content-Type", "application/x-www-form-urlencoded"),
            "patient={patient}",
            verifier.ids().patient()),
        // Multiple Date Parameters
        test(
            200,
            Observation.Bundle.class,
            "Observation/_search",
            Map.of("Content-Type", "application/x-www-form-urlencoded"),
            "patient={patient}&code={loinc1}&date={from}&date={to}",
            verifier.ids().patient(),
            verifier.ids().observations().loinc1(),
            verifier.ids().observations().dateRange().from(),
            verifier.ids().observations().dateRange().to()),
        // Search by _id
        test(
            200,
            Patient.Bundle.class,
            "Patient/_search",
            Map.of("Content-Type", "application/x-www-form-urlencoded"),
            "_id={id}",
            verifier.ids().patient()));
  }
}
