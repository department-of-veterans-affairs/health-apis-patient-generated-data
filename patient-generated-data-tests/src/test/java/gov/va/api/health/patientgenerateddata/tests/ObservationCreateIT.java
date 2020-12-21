package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPost;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.serializePayload;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Observation.ObservationStatus;
import gov.va.api.health.sentinel.Environment;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ObservationCreateIT {

  @Test
  public void create_invalid() {
    assumeEnvironmentIn(Environment.LOCAL);
    Observation observation = observation().id("123");
    doPost(
        "Observation", serializePayload(observation), "create invalid resource, existing id", 400);
  }

  @Test
  public void create_valid() {
    assumeEnvironmentIn(Environment.LOCAL);
    Observation observation = observation();
    doPost("Observation", serializePayload(observation), "create resource", 201);
  }

  private Observation observation() {
    return Observation.builder()
        .status(ObservationStatus.unknown)
        .effectiveDateTime("2020-01-01T01:00:00Z")
        .category(
            List.of(
                CodeableConcept.builder()
                    .coding(
                        List.of(
                            Coding.builder()
                                .system(
                                    "http://terminology.hl7.org/CodeSystem/observation-category")
                                .code("laboratory")
                                .build()))
                    .text("laboratory")
                    .build()))
        .code(CodeableConcept.builder().text("code").build())
        .subject(Reference.builder().reference("Patient/1").build())
        .build();
  }
}
