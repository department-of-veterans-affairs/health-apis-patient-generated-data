package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.BulkFhirCount;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.BadSearchParameter;
import gov.va.api.health.dstu2.api.resources.Patient;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class PatientBulkFhirControllerTest {
  @Autowired private TestEntityManager entityManager;

  @Autowired private PatientRepositoryV2 repository;

  @SneakyThrows
  private PatientEntityV2 asEntity(DatamartPatient dm) {
    return PatientEntityV2.builder()
        .icn(dm.fullIcn())
        .fullName(dm.name())
        .lastName(dm.lastName())
        .firstName(dm.firstName())
        .gender(dm.gender())
        .birthDate(parseInstant(dm.birthDateTime()))
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  PatientBulkFhirController controller() {
    return new PatientBulkFhirController(10, repository);
  }

  @Test
  public void count() {
    populateData();
    assertThat(controller().count())
        .isEqualTo(
            BulkFhirCount.builder()
                .resourceType("Patient")
                .count(10)
                .maxRecordsPerPage(10)
                .build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  private List<Patient> populateData() {
    var fhir = PatientSamples.Dstu2.create();
    var datamart = PatientSamples.Datamart.create();
    var patients = new ArrayList<Patient>();
    for (int i = 0; i < 10; i++) {
      var id = String.valueOf(i);
      var dm = datamart.patient(id);
      var entity = asEntity(dm);
      entityManager.persistAndFlush(entity);
      var patient = fhir.patient(id);
      patients.add(patient);
    }
    return patients;
  }

  @Test
  public void search() {
    List<Patient> patients = populateData();
    assertThat(json(controller().search(1, 10))).isEqualTo(json(patients));
  }

  @Test
  public void searchBadCountThrowsUnsatisfiedServletRequestParameterException() {
    assertThrows(BadSearchParameter.class, () -> controller().search(1, -1));
  }

  @Test
  public void searchBadPageThrowsUnsatisfiedServletRequestParameterException() {
    assertThrows(BadSearchParameter.class, () -> controller().search(0, 15));
  }

  @Test
  public void searchForEmptyPage() {
    populateData();
    assertThat(json(controller().search(2, 10))).isEqualTo(json(Lists.emptyList()));
  }

  @Test
  public void searchForSmallPagesSumsToLargerPage() {
    List<Patient> patients = populateData();
    ArrayList<Patient> sumPatients = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      sumPatients.add(controller().search(i, 1).get(0));
    }
    assertThat(json(patients)).isEqualTo(json(sumPatients));
  }
}
