package gov.va.api.health.patientgenerateddata.patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.ReferenceQualifier;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;

public class PatientControllerTest {
  @Test
  void initDirectFieldAccess() {
    new PatientController(mock(PatientRepository.class), mock(ReferenceQualifier.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  @Test
  @SneakyThrows
  void read() {
    PatientRepository repo = mock(PatientRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    String payload =
        JacksonConfig.createMapper().writeValueAsString(Patient.builder().id("x").build());
    when(repo.findById("x"))
        .thenReturn(Optional.of(PatientEntity.builder().id("x").payload(payload).build()));
    assertThat(new PatientController(repo, qualifier).read("x"))
        .isEqualTo(Patient.builder().id("x").build());
  }

  @Test
  void read_notFound() {
    PatientRepository repo = mock(PatientRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    assertThrows(
        Exceptions.NotFound.class, () -> new PatientController(repo, qualifier).read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    PatientRepository repo = mock(PatientRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    Patient patient = Patient.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(patient);
    when(repo.findById("x"))
        .thenReturn(Optional.of(PatientEntity.builder().id("x").payload(payload).build()));
    assertThat(new PatientController(repo, qualifier).update("x", patient))
        .isEqualTo(ResponseEntity.ok().build());
    verify(repo, times(1)).save(PatientEntity.builder().id("x").payload(payload).build());
  }

  @Test
  @SneakyThrows
  void update_new() {
    PatientRepository repo = mock(PatientRepository.class);
    ReferenceQualifier qualifier = mock(ReferenceQualifier.class);
    Patient patient = Patient.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(patient);
    assertThat(new PatientController(repo, qualifier).update("x", patient))
        .isEqualTo(ResponseEntity.status(HttpStatus.CREATED).build());
    verify(repo, times(1)).save(PatientEntity.builder().id("x").payload(payload).build());
  }
}
