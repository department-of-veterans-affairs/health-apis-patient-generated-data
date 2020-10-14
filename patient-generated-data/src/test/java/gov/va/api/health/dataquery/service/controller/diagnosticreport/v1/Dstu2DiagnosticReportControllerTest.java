package gov.va.api.health.dataquery.service.controller.diagnosticreport.v1;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static gov.va.api.health.dataquery.service.controller.Transformers.parseInstant;
import static gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples.DatamartV1;
import static gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples.Dstu2;
import static gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples.Dstu2.link;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Iterables;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.diagnosticreport.Dstu2DiagnosticReportController;
import gov.va.api.health.dstu2.api.DataAbsentReason;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.DiagnosticReport;
import gov.va.api.health.ids.api.IdentityService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class Dstu2DiagnosticReportControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  @Autowired private TestEntityManager entityManager;

  public Dstu2DiagnosticReportController controller() {
    return new Dstu2DiagnosticReportController(
        new Dstu2Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        WitnessProtection.builder().identityService(mock(IdentityService.class)).build(),
        null,
        entityManager.getEntityManager());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  public void read() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport report = controller().read(false, "800260864479:L");
    assertThat(report).isEqualTo(Dstu2.create().report());
  }

  @Test
  public void readForEmptyReportShouldExplodeNotFound() {
    Dstu2DiagnosticReportController controller = controller();
    assertThrows(ResourceExceptions.NotFound.class, () -> controller.read(false, "800260864479:L"));
  }

  @Test
  public void readForUnknownReportShouldExplodeNotFound() {
    Dstu2DiagnosticReportController controller = controller();
    assertThrows(ResourceExceptions.NotFound.class, () -> controller.read(false, "123456:X"));
  }

  @Test
  @SneakyThrows
  public void readRaw() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    String report = controller().readRaw(false, "800260864479:L", response);
    assertThat(
            JacksonConfig.createMapper()
                .readValue(report, DatamartDiagnosticReports.DiagnosticReport.class))
        .isEqualTo(dm.report());
    verify(response).addHeader("X-VA-INCLUDES-ICN", dm.entity().icn());
  }

  @Test
  public void readRawForEmptyReportShouldExplodeNotFound() {
    Dstu2DiagnosticReportController controller = controller();
    assertThrows(
        ResourceExceptions.NotFound.class,
        () -> controller.readRaw(false, "800260864479:L", response));
  }

  @Test
  public void readRawForUnknownReportShouldExplodeNotFound() {
    Dstu2DiagnosticReportController controller = controller();
    assertThrows(
        ResourceExceptions.NotFound.class, () -> controller.readRaw(false, "123456:X", response));
  }

  @Test
  public void readRawWithNoReport() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entityWithNoReport());
    entityManager.persistAndFlush(dm.crossEntity());
    assertThrows(
        ResourceExceptions.NotFound.class,
        () -> controller().readRaw(false, "800260864479:L", response));
  }

  @Test
  public void searchById() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle = controller().searchById(false, "800260864479:L", 1, 15);
    validateSearchByIdResult(bundle);
  }

  @Test
  public void searchByIdWith0Count() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle = controller().searchById(false, "800260864479:L", 1, 0);
    String encoded = URLEncoder.encode("800260864479:L", StandardCharsets.UTF_8);
    assertThat(json(bundle))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    1,
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=" + encoded,
                        1,
                        0))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle =
        controller().searchByIdentifier(false, "800260864479:L", 1, 15);
    validateSearchByIdResult(bundle);
  }

  @Test
  public void searchByPatient() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    DiagnosticReport.Bundle bundle =
        controller().searchByPatient(false, "1011537977V693883", 1, 15);
    assertThat(Iterables.getOnlyElement(bundle.entry()).resource())
        .isEqualTo(Dstu2.create().report());
    assertThat(json(bundle))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(Dstu2.create().report()),
                    1,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?patient=1011537977V693883",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?patient=1011537977V693883",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?patient=1011537977V693883",
                        1,
                        15))));
  }

  @Test
  @SneakyThrows
  public void searchByPatientAndCategoryAndDate() {
    String icn = "1011537977V693883";
    String reportId1 = "1:L";
    String reportId2 = "2:L";
    String time1 = "2009-09-24T03:15:24";
    String time2 = "2009-09-25T03:15:24";
    DiagnosticReportsEntity entity =
        DiagnosticReportsEntity.builder()
            .icn(icn)
            .payload(
                createMapper()
                    .writeValueAsString(
                        DatamartDiagnosticReports.builder()
                            .fullIcn(icn)
                            .reports(
                                asList(
                                    DatamartDiagnosticReports.DiagnosticReport.builder()
                                        .identifier(reportId1)
                                        .effectiveDateTime(time1)
                                        .build(),
                                    DatamartDiagnosticReports.DiagnosticReport.builder()
                                        .identifier(reportId2)
                                        .effectiveDateTime(time2)
                                        .build()))
                            .build()))
            .build();
    entityManager.persistAndFlush(entity);
    DiagnosticReport.Bundle bundle =
        controller()
            .searchByPatientAndCategory(
                false,
                icn,
                "LAB",
                new String[] {"gt2008", "ge2008", "eq2009", "le2010", "lt2010"},
                1,
                15);
    assertThat(bundle.entry().stream().map(e -> e.resource()).collect(Collectors.toList()))
        .isEqualTo(
            asList(
                DiagnosticReport.builder()
                    .id(reportId2)
                    .resourceType("DiagnosticReport")
                    .status(DiagnosticReport.Code._final)
                    .category(
                        CodeableConcept.builder()
                            .coding(
                                asList(
                                    Coding.builder()
                                        .system(
                                            "http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                                        .code("LAB")
                                        .display("Laboratory")
                                        .build()))
                            .build())
                    .code(CodeableConcept.builder().text("panel").build())
                    .subject(Reference.builder().reference("Patient/" + icn).build())
                    .effectiveDateTime(parseInstant(time2).toString())
                    ._performer(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
                    .build(),
                DiagnosticReport.builder()
                    .id(reportId1)
                    .resourceType("DiagnosticReport")
                    .status(DiagnosticReport.Code._final)
                    .category(
                        CodeableConcept.builder()
                            .coding(
                                asList(
                                    Coding.builder()
                                        .system(
                                            "http://hl7.org/fhir/ValueSet/diagnostic-service-sections")
                                        .code("LAB")
                                        .display("Laboratory")
                                        .build()))
                            .build())
                    .code(CodeableConcept.builder().text("panel").build())
                    .subject(Reference.builder().reference("Patient/" + icn).build())
                    .effectiveDateTime(parseInstant(time1).toString())
                    ._performer(DataAbsentReason.of(DataAbsentReason.Reason.unknown))
                    .build()));
  }

  @Test
  public void searchByPatientAndCategoryAndDateForCategoryWithNoResults() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle =
        controller()
            .searchByPatientAndCategory(false, "1011537977V693883", "CHEM", new String[] {}, 1, 15);
    // Searching for any category except LAB yields no results
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  public void searchByPatientAndCategoryAndDateForDateWithNoResults() {
    DatamartV1 dm = DatamartV1.builder().effectiveDateTime("").issuedDateTime("").build();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle =
        controller()
            .searchByPatientAndCategory(
                false, "1011537977V693883", "LAB", new String[] {"ge2000"}, 1, 15);
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  public void searchByPatientAndCategoryAndDateWithDateGreaterThan() {
    DatamartV1 dm =
        DatamartV1.builder()
            .issuedDateTime("2009-09-24T00:00:00")
            .effectiveDateTime("2009-09-24T01:00:00")
            .build();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    Dstu2DiagnosticReportController controller = controller();
    assertThat(
            controller
                .searchByPatientAndCategory(
                    false,
                    "1011537977V693883",
                    "LAB",
                    new String[] {"gt2009-09-24T00:00:00Z"},
                    1,
                    15)
                .entry()
                .size())
        .isEqualTo(1);
    assertThat(
            controller
                .searchByPatientAndCategory(
                    false,
                    "1011537977V693883",
                    "LAB",
                    new String[] {"gt2009-09-24T01:00:00Z"},
                    1,
                    15)
                .entry())
        .isEmpty();
  }

  @Test
  public void searchByPatientAndCategoryAndDateWithExactEffectiveMatch() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle =
        controller()
            .searchByPatientAndCategory(
                false, "1011537977V693883", "LAB", new String[] {"2009-09-24T03:15:24Z"}, 1, 15);
    assertThat(bundle.entry().size()).isEqualTo(1);
  }

  @Test
  public void searchByPatientAndCategoryAndDateWithExactIssuedTimeMatch() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle =
        controller()
            .searchByPatientAndCategory(
                false, "1011537977V693883", "LAB", new String[] {"2009-09-24T03:36:35Z"}, 1, 15);
    assertThat(bundle.entry().size()).isEqualTo(1);
  }

  @Test
  public void searchByPatientAndCodeWithCodeGivenShouldGiveNoResults() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle =
        controller().searchByPatientAndCode(false, "1011537977V693883", "x", 1, 15);
    // Searching by any code yields no results
    assertThat(bundle.entry()).isEmpty();
  }

  @Test
  public void searchByPatientAndCodeWithNoCodeGiven() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    entityManager.persistAndFlush(dm.crossEntity());
    DiagnosticReport.Bundle bundle =
        controller().searchByPatientAndCode(false, "1011537977V693883", "", 1, 15);
    assertThat(Iterables.getOnlyElement(bundle.entry()).resource())
        .isEqualTo(Dstu2.create().report());
    assertThat(json(bundle))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(Dstu2.create().report()),
                    1,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?code=&patient=1011537977V693883",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?code=&patient=1011537977V693883",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?code=&patient=1011537977V693883",
                        1,
                        15))));
  }

  @Test
  public void searchByPatientRaw() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    String json = controller().searchByPatientRaw("1011537977V693883", response);
    assertThat(
            DiagnosticReportsEntity.builder().payload(json).build().asDatamartDiagnosticReports())
        .isEqualTo(dm.reports());
    verify(response).addHeader("X-VA-INCLUDES-ICN", dm.entity().icn());
  }

  @Test
  public void searchByPatientRawForUnknownPatient() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    assertThrows(
        ResourceExceptions.NotFound.class,
        () -> controller().searchByPatientRaw("WHODIS?", response));
  }

  @Test
  public void searchByUnknownPatient() {
    DatamartV1 dm = DatamartV1.create();
    entityManager.persistAndFlush(dm.entity());
    DiagnosticReport.Bundle bundle = controller().searchByPatient(false, "WHODIS", 1, 15);
    assertThat(bundle.entry()).isEmpty();
  }

  private void validateSearchByIdResult(DiagnosticReport.Bundle bundle) {
    String encoded = URLEncoder.encode("800260864479:L", StandardCharsets.UTF_8);
    assertThat(Iterables.getOnlyElement(bundle.entry()).resource())
        .isEqualTo(Dstu2.create().report());
    assertThat(json(bundle))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(Dstu2.create().report()),
                    1,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=" + encoded,
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=" + encoded,
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?identifier=" + encoded,
                        1,
                        15))));
  }
}
