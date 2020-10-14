package gov.va.api.health.dataquery.service.controller.diagnosticreport.v1;

import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameters;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotFound;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.resources.DiagnosticReport;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Request Mappings for Diagnostic Report Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-diagnosticreport.html for
 * implementation details.
 */
@Validated
@Slf4j
@SuppressWarnings("WeakerAccess")
public class Dstu2DiagnosticReportController {
  private Dstu2Bundler bundler;

  private WitnessProtection witnessProtection;

  private EntityManager entityManager;

  /** All args constructor. */
  public Dstu2DiagnosticReportController(
      @Autowired Dstu2Bundler bundler,
      @Autowired WitnessProtection witnessProtection,
      @Autowired EntityManager entityManager) {
    this.bundler = bundler;
    this.witnessProtection = witnessProtection;
    this.entityManager = entityManager;
  }

  private static boolean dateParameterIsSatisfied(
      DatamartDiagnosticReports.DiagnosticReport report, DateTimeParameters parameter) {
    Instant effective = parseInstant(trimToEmpty(report.effectiveDateTime()));
    if (effective != null
        && parameter.isSatisfied(effective.toEpochMilli(), effective.toEpochMilli())) {
      return true;
    }
    Instant issued = parseInstant(trimToEmpty(report.issuedDateTime()));
    if (issued != null && parameter.isSatisfied(issued.toEpochMilli(), issued.toEpochMilli())) {
      return true;
    }
    return false;
  }

  private static List<DatamartDiagnosticReports.DiagnosticReport> filterDates(
      List<DatamartDiagnosticReports.DiagnosticReport> datamartReports,
      List<String> dateParameters) {
    if (isEmpty(dateParameters)) {
      return datamartReports;
    }
    List<DateTimeParameters> parameters =
        dateParameters.stream().map(DateTimeParameters::new).collect(Collectors.toList());
    return datamartReports.stream()
        .filter(r -> parameters.stream().allMatch(p -> dateParameterIsSatisfied(r, p)))
        .collect(Collectors.toList());
  }

  private DiagnosticReport.Bundle bundle(
      MultiValueMap<String, String> parameters, List<DiagnosticReport> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("DiagnosticReport")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig, reports, DiagnosticReport.Entry::new, DiagnosticReport.Bundle::new));
  }

  @SneakyThrows
  private Pair<DatamartDiagnosticReports, DatamartDiagnosticReports.DiagnosticReport> pairPayload(
      String publicId) {
    MultiValueMap<String, String> publicParameters = Parameters.forIdentity(publicId);
    MultiValueMap<String, String> cdwParameters =
        witnessProtection.replacePublicIdsWithCdwIds(publicParameters);
    String cdwReportId = Parameters.identiferOf(cdwParameters);
    TypedQuery<DiagnosticReportsEntity> query =
        entityManager.createQuery(
            "SELECT dr FROM DiagnosticReportCrossEntity drc join"
                + " DiagnosticReportsEntity dr on"
                + " dr.icn = drc.icn where drc.reportId = :identifier",
            DiagnosticReportsEntity.class);

    query.setParameter("identifier", cdwReportId);
    query.setMaxResults(1);
    log.info("Starting Diagnostic Report Read");
    List<DiagnosticReportsEntity> entities = query.getResultList();
    log.info("Finished Diagnostic Report Read");
    if (isEmpty(entities)) {
      throw new ResourceExceptions.NotFound(publicParameters);
    }
    DatamartDiagnosticReports payload =
        Iterables.getOnlyElement(entities).asDatamartDiagnosticReports();
    Optional<DatamartDiagnosticReports.DiagnosticReport> maybeReport =
        payload.reports().stream()
            .filter(r -> StringUtils.equals(r.identifier(), cdwReportId))
            .findFirst();
    if (!maybeReport.isPresent()) {
      throw new ResourceExceptions.NotFound(publicParameters);
    }
    return Pair.of(payload, maybeReport.get());
  }

  /** Read by identifier. */
  public DiagnosticReport read(@PathVariable("publicId") String publicId) {
    Pair<DatamartDiagnosticReports, DatamartDiagnosticReports.DiagnosticReport> result =
        pairPayload(publicId);
    DatamartDiagnosticReports payload = result.getFirst();
    DatamartDiagnosticReports.DiagnosticReport report = result.getSecond();
    replaceCdwIdsWithPublicIds(asList(report));
    return Dstu2DiagnosticReportTransformer.builder()
        .datamart(report)
        .icn(payload.fullIcn())
        .patientName(payload.patientName())
        .build()
        .toFhir();
  }

  /** Return the raw Datamart document for the given identifier. */
  public DatamartDiagnosticReports.DiagnosticReport readRaw(
      @PathVariable("publicId") String publicId, HttpServletResponse response) {
    var pair = pairPayload(publicId);
    IncludesIcnMajig.addHeader(response, pair.getFirst().fullIcn());
    return pair.getSecond();
  }

  private void replaceCdwIdsWithPublicIds(
      List<DatamartDiagnosticReports.DiagnosticReport> reports) {
    Set<ResourceIdentity> ids =
        reports.stream()
            .flatMap(
                report ->
                    Stream.concat(
                        Stream.of(
                            ResourceIdentity.builder()
                                .system("CDW")
                                .resource("DIAGNOSTIC_REPORT")
                                .identifier(report.identifier())
                                .build(),
                            report.accessionInstitutionSid() != null
                                ? ResourceIdentity.builder()
                                    .system("CDW")
                                    .resource("ORGANIZATION")
                                    .identifier(report.accessionInstitutionSid())
                                    .build()
                                : null),
                        report.results().stream()
                            .flatMap(
                                r ->
                                    Stream.of(
                                        ResourceIdentity.builder()
                                            .system("CDW")
                                            .resource("OBSERVATION")
                                            .identifier(r.result())
                                            .build()))))
            .filter(r -> r != null)
            .collect(Collectors.toSet());

    List<Registration> registrations = witnessProtection.register(ids);
    final Table<String, String, String> idsTable = HashBasedTable.create();
    for (Registration r : registrations) {
      for (ResourceIdentity id : r.resourceIdentities()) {
        idsTable.put(id.identifier(), id.resource(), r.uuid());
      }
    }
    for (DatamartDiagnosticReports.DiagnosticReport report : reports) {
      String identifier = idsTable.get(report.identifier(), "DIAGNOSTIC_REPORT");
      if (identifier != null) {
        report.identifier(identifier);
      }
      String accessionInstitutionSid =
          idsTable.get(report.accessionInstitutionSid(), "ORGANIZATION");
      if (accessionInstitutionSid != null) {
        report.accessionInstitutionSid(accessionInstitutionSid);
      }
      for (var result : report.results()) {
        String resultId = idsTable.get(result.result(), "OBSERVATION");
        if (resultId != null) {
          result.result(resultId);
        }
      }
    }
  }

  @SneakyThrows
  private DiagnosticReport.Bundle search(MultiValueMap<String, String> publicParameters) {
    MultiValueMap<String, String> cdwParameters =
        witnessProtection.replacePublicIdsWithCdwIds(publicParameters);
    // Filter category
    String category = cdwParameters.getFirst("category");
    if (isNotBlank(category) && !equalsIgnoreCase(category, "LAB")) {
      // All diagnostic reports are labs
      return bundle(publicParameters, emptyList(), 0);
    }
    // Filter code
    String code = cdwParameters.getFirst("code");
    if (isNotBlank(code)) {
      // LOINC codes are not available in CDW
      return bundle(publicParameters, emptyList(), 0);
    }
    log.info("Starting Diagnostic Report search by patient");
    DiagnosticReportsEntity entity =
        entityManager.find(DiagnosticReportsEntity.class, cdwParameters.getFirst("patient"));
    log.info("Finished Diagnostic Report search by patient");
    if (entity == null) {
      return bundle(publicParameters, emptyList(), 0);
    }
    DatamartDiagnosticReports payload = entity.asDatamartDiagnosticReports();
    List<DatamartDiagnosticReports.DiagnosticReport> filtered =
        filterDates(payload.reports(), cdwParameters.get("date"));
    // Most recent reports first
    Collections.sort(
        filtered,
        (left, right) -> {
          int result = StringUtils.compare(right.effectiveDateTime(), left.effectiveDateTime());
          if (result != 0) {
            return result;
          }
          return StringUtils.compare(right.issuedDateTime(), left.issuedDateTime());
        });
    int page = Parameters.pageOf(cdwParameters);
    int count = Parameters.countOf(cdwParameters);
    int fromIndex = Math.min((page - 1) * count, filtered.size());
    int toIndex = Math.min(fromIndex + count, filtered.size());
    List<DatamartDiagnosticReports.DiagnosticReport> paged = filtered.subList(fromIndex, toIndex);
    replaceCdwIdsWithPublicIds(paged);
    List<DiagnosticReport> fhir =
        paged.stream()
            .map(
                dm ->
                    Dstu2DiagnosticReportTransformer.builder()
                        .datamart(dm)
                        .icn(payload.fullIcn())
                        .patientName(payload.patientName())
                        .build()
                        .toFhir())
            .collect(Collectors.toList());
    return bundle(publicParameters, fhir, filtered.size());
  }

  /** Search by _id. */
  public DiagnosticReport.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build();
    DiagnosticReport report = read(id);
    int totalRecords = report == null ? 0 : 1;
    if (report == null || page != 1 || count <= 0) {
      return bundle(parameters, emptyList(), totalRecords);
    }
    return bundle(parameters, asList(report), totalRecords);
  }

  /** Search by identifier. */
  public DiagnosticReport.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(identifier, page, count);
  }

  /** Search by patient. */
  public DiagnosticReport.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return search(
        Parameters.builder()
            .add("patient", patient)
            .add("page", page)
            .add("_count", count)
            .build());
  }

  /** Search by Patient+Category + Date if provided. */
  public DiagnosticReport.Bundle searchByPatientAndCategoryAndDate(
      @RequestParam("patient") String patient,
      @RequestParam("category") String category,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return search(
        Parameters.builder()
            .add("patient", patient)
            .add("category", category)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build());
  }

  /** Search by Patient+Code. */
  public DiagnosticReport.Bundle searchByPatientAndCode(
      @RequestParam("patient") String patient,
      @RequestParam("code") String code,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return search(
        Parameters.builder()
            .add("patient", patient)
            .add("code", code)
            .add("page", page)
            .add("_count", count)
            .build());
  }

  /** Search by patient. */
  public String searchByPatientRaw(
      @RequestParam("patient") String patient, HttpServletResponse response) {
    MultiValueMap<String, String> publicParameters =
        Parameters.builder().add("patient", patient).build();
    MultiValueMap<String, String> cdwParameters =
        witnessProtection.replacePublicIdsWithCdwIds(publicParameters);
    DiagnosticReportsEntity entity =
        entityManager.find(DiagnosticReportsEntity.class, cdwParameters.getFirst("patient"));
    if (entity == null) {
      throw new NotFound(publicParameters);
    }
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }
}
