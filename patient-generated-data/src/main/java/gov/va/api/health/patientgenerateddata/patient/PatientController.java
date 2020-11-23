package gov.va.api.health.patientgenerateddata.patient;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.SerializationUtils.deserializedPayload;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.autoconfig.logging.Loggable;
import gov.va.api.health.patientgenerateddata.Exceptions;
import gov.va.api.health.patientgenerateddata.ReferenceQualifier;
import gov.va.api.health.r4.api.resources.Patient;
import java.util.Optional;
import java.util.stream.Stream;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
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
  private final PatientRepository repository;

  private final ReferenceQualifier referenceQualifier;

  @InitBinder
  void initDirectFieldAccess(DataBinder dataBinder) {
    dataBinder.initDirectFieldAccess();
  }

  @GetMapping(value = "/{id}")
  Patient read(@PathVariable("id") String id) {
    Optional<PatientEntity> maybeEntity = repository.findById(id);
    PatientEntity entity = maybeEntity.orElseThrow(() -> new Exceptions.NotFound(id));
    // referenceQualifier.serializeAsField(entity.payload());
    referenceQualifier.qualify(Stream.empty());
    return deserializedPayload(id, entity.payload(), Patient.class);
  }

  @SneakyThrows
  @PutMapping(value = "/{id}")
  @Loggable(arguments = false)
  ResponseEntity<Void> update(@PathVariable("id") String id, @Valid @RequestBody Patient patient) {
    String payload = JacksonConfig.createMapper().writeValueAsString(patient);
    checkState(id.equals(patient.id()), "%s != %s", id, patient.id());
    Optional<PatientEntity> maybeEntity = repository.findById(id);
    if (maybeEntity.isPresent()) {
      PatientEntity entity = maybeEntity.get();
      entity.payload(payload);
      repository.save(entity);
      return ResponseEntity.ok().build();
    }
    repository.save(PatientEntity.builder().id(id).payload(payload).build());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
