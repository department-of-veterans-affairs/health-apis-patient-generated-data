package gov.va.api.health.patientgenerateddata.tests;

import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.doPost;
import static gov.va.api.health.patientgenerateddata.tests.RequestUtils.serializePayload;
import static gov.va.api.health.patientgenerateddata.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;

import gov.va.api.health.r4.api.datatypes.HumanName;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Patient.Gender;
import gov.va.api.health.sentinel.Environment;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PatientCreateIT {
  @Test
  public void create_invalid() {
    assumeEnvironmentIn(Environment.LOCAL);
    // not an mpi id
    String id = "123";
    Patient patient = patient(id);
    doPost("Patient", serializePayload(patient), "create invalid resource, bad mpi format", 400);
    // no id
    patient = patient(null);
    doPost("Patient", serializePayload(patient), "create invalid resource, missing id", 400);
    // mismatch mpi id and identifier
    id = "1111111111V111111";
    patient = patient(id);
    patient.identifier().add(identifier().system("http://va.gov/mpi"));
    doPost(
        "Patient", serializePayload(patient), "create invalid resource, mismatching MPI ids", 400);
  }

  @Test
  public void create_valid() {
    assumeEnvironmentIn(Environment.LOCAL);
    var id = systemDefinition().ids().patientGenerated();
    Patient patient = patient(id);
    doPost("Patient", serializePayload(patient), "create resource", 201);
  }

  Identifier identifier() {
    return Identifier.builder().system("http://foo.com/foo").value("foo").build();
  }

  Patient patient(String id) {
    return Patient.builder()
        .resourceType("Patient")
        .id(id)
        .identifier(new ArrayList<>(List.of(identifier())))
        .name(List.of(HumanName.builder().family("Smith").build()))
        .gender(Gender.unknown)
        .build();
  }
}