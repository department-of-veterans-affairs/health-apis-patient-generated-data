package gov.va.api.health.patientgenerateddata.patient;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.LinkProperties;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Identifier;
import gov.va.api.health.r4.api.datatypes.Identifier.IdentifierUse;
import gov.va.api.health.r4.api.resources.Patient;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.validation.Valid;
import liquibase.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(
    value = "/r4/Patient",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class PatientController {
  private final LinkProperties linkProperties;

  private final PatientRepository repository;

  @PostMapping
  ResponseEntity<Patient> create(@Valid @RequestBody Patient patient) {
    checkRequestState(!StringUtils.isEmpty(patient.id()), "Patient ICN is required in id field");
    String id = patient.id();
    checkRequestState(isValidIcn(id), "Patient ICN must be in valid MPI format.");
    String icnIdentifier = findIcn(patient);
    if (icnIdentifier != null) {
      checkRequestState(
          id.equals(icnIdentifier), "Identifiers don't match, %s != %s", id, icnIdentifier);
    } else {
      Identifier mpiIdentifier =
          Identifier.builder()
              .use(IdentifierUse.usual)
              .type(
                  CodeableConcept.builder()
                      .coding(
                          Collections.singletonList(
                              Coding.builder()
                                  .system("http://hl7.org/fhir/v2/0203")
                                  .code("MR")
                                  .build()))
                      .build())
              .system("http://va.gov/mpi")
              .value(id)
              .build();
      patient.identifier().add(mpiIdentifier);
    }
    return update(id, patient);
  }

  private String findIcn(Patient patient) {
    checkState(patient != null);
    if (patient.identifier() != null) {
      Optional<Identifier> mpi =
          patient.identifier().stream()
              .filter(
                  (identifier) -> {
                    if (identifier.system() != null) {
                      return identifier.system().equals("http://va.gov/mpi");
                    }
                    return false;
                  })
              .findFirst();
      if (mpi.isPresent()) {
        return mpi.get().value();
      }
    }
    return null;
  }

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @SneakyThrows
  private boolean isValidIcn(String icn) {
    checkState(!StringUtils.isEmpty(icn));
    Pattern pattern = Pattern.compile("^[0-9]{10}V[0-9]{6}$");
    return pattern.matcher(icn.trim()).matches();
  }

  @GetMapping(value = "/{id}")
  Patient read(@PathVariable("id") String id) {
    Optional<PatientEntity> maybeEntity = repository.findById(id);
    PatientEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    return entity.deserializePayload();
  }

  @SneakyThrows
  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Patient> update(
      @PathVariable("id") String id, @Valid @RequestBody Patient patient) {
    String payload = JacksonConfig.createMapper().writeValueAsString(patient);
    checkState(id.equals(patient.id()), "%s != %s", id, patient.id());
    Optional<PatientEntity> maybeEntity = repository.findById(id);
    if (maybeEntity.isPresent()) {
      PatientEntity entity = maybeEntity.get();
      entity.payload(payload);
      repository.save(entity);
      return ResponseEntity.ok(patient);
    }
    repository.save(PatientEntity.builder().id(id).payload(payload).build());
    return ResponseEntity.created(URI.create(linkProperties.r4Url() + "/Patient/" + id))
        .body(patient);
  }
}
