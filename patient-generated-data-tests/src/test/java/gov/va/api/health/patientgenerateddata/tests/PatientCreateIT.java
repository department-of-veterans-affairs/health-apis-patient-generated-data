package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPost;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.sentinel.Environment;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PatientCreateIT {
  @BeforeAll
  static void assumeEnvironment() {
    // These tests invent data that will not be cleaned up
    // To avoid polluting the database, they should only run locally
    assumeEnvironmentIn(Environment.LOCAL);
  }

  static Identifier identifier() {
    return Identifier.builder().system("http://foo.com/foo").value("foo").build();
  }

  static Patient patient(String id) {
    return Patient.builder()
        .resourceType("Patient")
        .id(id)
        .identifier(new ArrayList<>(List.of(identifier())))
        .name(List.of(HumanName.builder().family("Smith").build()))
        .gender(Patient.Gender.unknown)
        .build();
  }

  @Test
  void create_invalid() {
    // not an mpi id
    String id = "123";
    Patient patient = patient(id);
    doPost("Patient", patient, "create invalid resource, bad mpi format", 400);
    // no id
    patient = patient(null);
    doPost("Patient", patient, "create invalid resource, missing id", 400);
    // mismatch mpi id and identifier
    id = "1111111111V111111";
    patient = patient(id);
    patient.identifier().add(identifier().system("http://va.gov/mpi"));
    doPost("Patient", patient, "create invalid resource, mismatching MPI ids", 400);
  }

  @Test
  void create_valid() {
    var id = systemDefinition().ids().patientGenerated();
    Patient patient = patient(id);
    doPost("Patient", patient, "create resource", 201);
    // duplicate is rejected
    doPost("Patient", patient, "create resource (duplicate)", 400);
  }
}
