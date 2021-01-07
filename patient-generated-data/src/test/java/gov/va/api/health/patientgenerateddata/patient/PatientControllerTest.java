package gov.va.api.health.patientgenerateddata.patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.r4.api.resources.Patient;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;

public class PatientControllerTest {
  @Test
  @SneakyThrows
  void create() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    String id = "9999999999V999999";
    PatientRepository repo = mock(PatientRepository.class);
    Patient patient =
        Patient.builder()
            .id(id)
            .identifier(new ArrayList<>(List.of(Identifier.builder().build())))
            .build();
    assertThat(new PatientController(pageLinks, repo).create(patient))
        .isEqualTo(
            ResponseEntity.created(URI.create("http://foo.com/r4/Patient/" + id)).body(patient));
    Patient persisted =
        Patient.builder()
            .id(id)
            .identifier(new ArrayList<>(List.of(Identifier.builder().build(), mpi(id))))
            .build();
    String persistedSerialized = JacksonConfig.createMapper().writeValueAsString(persisted);
    verify(repo, times(1))
        .save(PatientEntity.builder().id(id).payload(persistedSerialized).build());
  }

  @Test
  void create_duplicate() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    String id = "9999999999V999999";
    PatientRepository repo = mock(PatientRepository.class);
    when(repo.findById(id))
        .thenReturn(Optional.of(PatientEntity.builder().id(id).payload("payload").build()));

    Patient patient =
        Patient.builder()
            .id(id)
            .identifier(new ArrayList<>(List.of(Identifier.builder().build())))
            .build();

    assertThrows(
        Exceptions.BadRequest.class, () -> new PatientController(pageLinks, repo).create(patient));
    verify(repo, times(0)).save(any());
  }

  @Test
  @SneakyThrows
  void create_invalid() {
    String id = "9V9";
    PatientRepository repo = mock(PatientRepository.class);
    Patient patient =
        Patient.builder()
            .id(id)
            .identifier(new ArrayList<>(List.of(Identifier.builder().build())))
            .build();
    assertThrows(
        Exceptions.BadRequest.class,
        () -> new PatientController(mock(LinkProperties.class), repo).create(patient));
  }

  @Test
  void initDirectFieldAccess() {
    new PatientController(mock(LinkProperties.class), mock(PatientRepository.class))
        .initDirectFieldAccess(mock(DataBinder.class));
  }

  private Identifier mpi(String icn) {
    return Identifier.builder()
        .use(IdentifierUse.usual)
        .type(
            CodeableConcept.builder()
                .coding(
                    Collections.singletonList(
                        Coding.builder().system("http://hl7.org/fhir/v2/0203").code("MR").build()))
                .build())
        .system("http://va.gov/mpi")
        .value(icn)
        .build();
  }

  @Test
  @SneakyThrows
  void read() {
    PatientRepository repo = mock(PatientRepository.class);
    String payload =
        JacksonConfig.createMapper().writeValueAsString(Patient.builder().id("x").build());
    when(repo.findById("x"))
        .thenReturn(Optional.of(PatientEntity.builder().id("x").payload(payload).build()));
    assertThat(new PatientController(mock(LinkProperties.class), repo).read("x"))
        .isEqualTo(Patient.builder().id("x").build());
  }

  @Test
  void read_notFound() {
    PatientRepository repo = mock(PatientRepository.class);
    assertThrows(
        Exceptions.NotFound.class,
        () -> new PatientController(mock(LinkProperties.class), repo).read("notfound"));
  }

  @Test
  @SneakyThrows
  void update_existing() {
    PatientRepository repo = mock(PatientRepository.class);
    Patient patient = Patient.builder().id("x").build();
    String payload = JacksonConfig.createMapper().writeValueAsString(patient);
    when(repo.findById("x"))
        .thenReturn(Optional.of(PatientEntity.builder().id("x").payload(payload).build()));
    assertThat(new PatientController(mock(LinkProperties.class), repo).update("x", patient))
        .isEqualTo(ResponseEntity.ok(patient));
    verify(repo, times(1)).save(PatientEntity.builder().id("x").payload(payload).build());
  }

  @Test
  @SneakyThrows
  void update_not_existing() {
    LinkProperties pageLinks =
        LinkProperties.builder().baseUrl("http://foo.com").r4BasePath("r4").build();
    PatientRepository repo = mock(PatientRepository.class);
    Patient patient = Patient.builder().id("x").build();
    assertThrows(
        Exceptions.NotFound.class,
        () -> new PatientController(pageLinks, repo).update("x", patient));
  }
}
