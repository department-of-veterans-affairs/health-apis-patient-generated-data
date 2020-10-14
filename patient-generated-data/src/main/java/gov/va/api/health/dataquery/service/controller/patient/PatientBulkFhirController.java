package gov.va.api.health.dataquery.service.controller.patient;

import gov.va.api.health.dataquery.service.controller.BulkFhirCount;
import gov.va.api.health.dataquery.service.controller.PageAndCountValidator;
import gov.va.api.health.dstu2.api.resources.Patient;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@ConditionalOnProperty("bulk-fhir.enabled")
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/internal/bulk/Patient"},
    produces = {"application/json", "application/fhir+json", "application/json+fhir"})
public class PatientBulkFhirController {

  private PatientRepositoryV2 repository;

  private int maxRecordsPerPage;

  /** All args constructor. */
  public PatientBulkFhirController(
      @Value("${bulk.patient.maxRecordsPerPage}") int maxRecordsPerPage,
      @Autowired PatientRepositoryV2 repository) {
    this.maxRecordsPerPage = maxRecordsPerPage;
    this.repository = repository;
  }

  /** Count by icn. */
  @GetMapping("/count")
  public BulkFhirCount count() {
    return BulkFhirCount.builder()
        .resourceType("Patient")
        .count(repository.count())
        .maxRecordsPerPage(maxRecordsPerPage)
        .build();
  }

  private PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count);
  }

  /** Get patients by page/count. */
  @GetMapping()
  public List<Patient> search(
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    PageAndCountValidator.validateCountBounds(count, maxRecordsPerPage);
    return repository.findAllProjectedBy(page(page, count)).stream()
        .map(PatientPayloadDto::asDatamartPatient)
        .map(dm -> Dstu2PatientTransformer.builder().datamart(dm).build().toFhir())
        .collect(Collectors.toList());
  }
}
