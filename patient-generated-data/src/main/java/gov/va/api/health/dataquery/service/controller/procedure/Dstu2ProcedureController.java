package gov.va.api.health.dataquery.service.controller.procedure;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.procedure.ProcedureRepository.PatientAndDateSpecification;
import gov.va.api.health.dstu2.api.resources.Procedure;
import gov.va.api.health.dstu2.api.resources.Procedure.Bundle;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Procedure Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-procedure.html for
 * implementation details.
 */
@Slf4j
@Builder
@Validated
@RestController
@RequestMapping(
    value = {"/dstu2/Procedure"},
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
public class Dstu2ProcedureController {

  private ProcedureHack procedureHack;

  private Dstu2Bundler bundler;

  private ProcedureRepository repository;

  private WitnessProtection witnessProtection;

  /** Let's try something new. */
  @Autowired
  public Dstu2ProcedureController(
      ProcedureHack procedureHack,
      Dstu2Bundler bundler,
      ProcedureRepository repository,
      WitnessProtection witnessProtection) {
    this.procedureHack = procedureHack;
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Bundle bundle(
      MultiValueMap<String, String> parameters, List<Procedure> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Procedure")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig, reports, Procedure.Entry::new, Procedure.Bundle::new));
  }

  ProcedureEntity findById(String publicId) {
    Optional<ProcedureEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Procedure read(
      @PathVariable("publicId") String publicId,
      @RequestHeader(value = "X-VA-ICN", required = false) String icnHeader) {
    DatamartProcedure procedure = findById(publicId).asDatamartProcedure();
    replaceReferences(List.of(procedure));
    Procedure fhir = transform(procedure);
    if (isNotBlank(icnHeader) && procedureHack.isPatientWithoutRecords(icnHeader)) {
      fhir = procedureHack.disguiseAsPatientWithoutRecords(fhir, Procedure.class);
    }
    return fhir;
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(
      @PathVariable("publicId") String publicId,
      @RequestHeader(value = "X-VA-ICN", required = false) String icnHeader,
      HttpServletResponse response) {
    ProcedureEntity entity = findById(publicId);
    if (isNotBlank(icnHeader)
        && procedureHack.isPatientWithoutRecords(icnHeader)
        && entity.icn().equals(procedureHack.withRecordsId)) {
      log.info("Procedure Hack: Setting includes header to magic patient: {}", sanitize(icnHeader));
      IncludesIcnMajig.addHeader(response, IncludesIcnMajig.encodeHeaderValue(icnHeader));
    } else {
      IncludesIcnMajig.addHeader(response, IncludesIcnMajig.encodeHeaderValue(entity.icn()));
    }
    return entity.payload();
  }

  Collection<DatamartProcedure> replaceReferences(Collection<DatamartProcedure> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources, resource -> Stream.of(resource.patient(), resource.location().orElse(null)));
    return resources;
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Procedure.Bundle searchById(
      @RequestHeader(value = "X-VA-ICN", required = false) String icnHeader,
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    Procedure resource = read(publicId, icnHeader);
    return bundle(
        Parameters.builder()
            .add("identifier", publicId)
            .add("page", page)
            .add("_count", count)
            .build(),
        resource == null || count == 0 ? emptyList() : List.of(resource),
        resource == null ? 0 : 1);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Procedure.Bundle searchByIdentifier(
      @RequestHeader(value = "X-VA-ICN", required = false) String icnHeader,
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(icnHeader, id, page, count);
  }

  /** Search by patient and date if provided. */
  @GetMapping(params = {"patient"})
  public Procedure.Bundle searchByPatientAndDate(
      @RequestParam("patient") String patient,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    final boolean isPatientWithoutRecords = procedureHack.isPatientWithoutRecords(patient);
    if (isPatientWithoutRecords) {
      patient = procedureHack.withRecordsId;
    }
    String icn = witnessProtection.toCdwId(patient);
    PatientAndDateSpecification spec =
        PatientAndDateSpecification.builder().patient(icn).dates(date).build();
    log.info("Looking for {} ({}) {}", patient, icn, spec);
    Page<ProcedureEntity> pageOfProcedures =
        repository.findAll(
            spec, PageRequest.of(page - 1, count == 0 ? 1 : count, ProcedureEntity.naturalOrder()));
    Bundle bundle;
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("patient", patient)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build();
    log.info("Search {} found {} results", parameters, pageOfProcedures.getTotalElements());
    if (count == 0) {
      bundle = bundle(parameters, emptyList(), (int) pageOfProcedures.getTotalElements());
    } else {
      bundle =
          bundle(
              parameters,
              replaceReferences(
                      pageOfProcedures
                          .get()
                          .map(ProcedureEntity::asDatamartProcedure)
                          .collect(Collectors.toList()))
                  .stream()
                  .map(this::transform)
                  .collect(Collectors.toList()),
              (int) pageOfProcedures.getTotalElements());
    }
    if (isPatientWithoutRecords) {
      bundle = procedureHack.disguiseAsPatientWithoutRecords(bundle, Bundle.class);
    }
    return bundle;
  }

  Procedure transform(DatamartProcedure dm) {
    return Dstu2ProcedureTransformer.builder().datamart(dm).build().toFhir();
  }
}
