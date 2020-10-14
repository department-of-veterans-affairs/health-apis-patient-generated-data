package gov.va.api.health.dataquery.service.controller.diagnosticreport;

import static gov.va.api.health.dataquery.service.controller.diagnosticreport.DiagnosticReportSamples.R4.link;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.DiagnosticReport;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class R4DiagnosticReportControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private DiagnosticReportRepository repository;

  private DiagnosticReport.Bundle bundleOf(
      Collection<DiagnosticReport> diagnosticReports, String paramString, int page, int count) {
    return DiagnosticReportSamples.R4.asBundle(
        "http://fonzy.com/cool",
        diagnosticReports,
        diagnosticReports.size(),
        link(
            BundleLink.LinkRelation.first,
            "http://fonzy.com/cool/DiagnosticReport?" + paramString,
            1,
            count),
        link(
            BundleLink.LinkRelation.self,
            "http://fonzy.com/cool/DiagnosticReport?" + paramString,
            1,
            count),
        link(
            BundleLink.LinkRelation.last,
            "http://fonzy.com/cool/DiagnosticReport?" + paramString,
            page,
            count));
  }

  private R4DiagnosticReportController controller() {
    return new R4DiagnosticReportController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        WitnessProtection.builder().identityService(ids).build(),
        repository);
  }

  private DiagnosticReport.Bundle emptyBundleOf(String paramString) {
    return bundleOf(emptyList(), paramString, 1, 15);
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  @SneakyThrows
  private DiagnosticReportEntity entity(DatamartDiagnosticReport dm) {
    return entity("CH", dm);
  }

  @SneakyThrows
  private DiagnosticReportEntity entity(String category, DatamartDiagnosticReport dm) {
    return DiagnosticReportEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().orElse(null))
        .category(category)
        .code("panel")
        .dateUtc(Instant.parse(dm.issuedDateTime()))
        .payload(json(dm))
        .build();
  }

  @SneakyThrows
  private String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  void mockDiagnosticReportEntity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder()
            .system("CDW")
            .resource("DIAGNOSTIC_REPORT")
            .identifier(cdwId)
            .build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  @SneakyThrows
  private DatamartDiagnosticReport object(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartDiagnosticReport.class);
  }

  private Multimap<String, DiagnosticReport> populateData() {
    var datamart = DiagnosticReportSamples.DatamartV2.create();
    var r4 = DiagnosticReportSamples.R4.create();
    var diagnosticReportsByPatient = LinkedHashMultimap.<String, DiagnosticReport>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      String date = "2020-01-2" + i + "T07:57:00Z";
      String category = i % 2 == 0 ? "MB" : "CH";
      String patientId = "p" + i % 2;
      String cdwId = "cdw-" + i;
      String publicId = "public" + i;
      DatamartDiagnosticReport dm = datamart.diagnosticReport(cdwId, patientId, date);
      repository.save(entity(category, dm));
      DiagnosticReport dr = r4.diagnosticReport(publicId, patientId, date);
      diagnosticReportsByPatient.put(patientId, dr);
      mockDiagnosticReportEntity(publicId, cdwId);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder()
              .system("CDW")
              .resource("DIAGNOSTIC_REPORT")
              .identifier(cdwId)
              .build();
      Registration registration =
          Registration.builder()
              .uuid(publicId)
              .resourceIdentities(List.of(resourceIdentity))
              .build();
      registrations.add(registration);
      when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    }
    when(ids.register(Mockito.any())).thenReturn(registrations);
    return diagnosticReportsByPatient;
  }

  @Test
  void read() {
    DatamartDiagnosticReport dm = DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
    repository.save(entity(dm));
    mockDiagnosticReportEntity("x", dm.cdwId());
    assertThat(controller().read("x"))
        .isEqualTo(DiagnosticReportSamples.R4.create().diagnosticReport("x"));
  }

  @Test
  void readRaw() {
    DatamartDiagnosticReport dm = DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
    DiagnosticReportEntity entity = entity(dm);
    repository.save(entity);
    mockDiagnosticReportEntity("x", dm.cdwId());
    assertThat(object(controller().readRaw("x", response))).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test
  void readUnknownIdIsNotFound() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("bigOOF"));
  }

  @Test
  void searchById() {
    DatamartDiagnosticReport dm = DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
    repository.save(entity(dm));
    mockDiagnosticReportEntity("x", dm.cdwId());
    DiagnosticReport r4 = DiagnosticReportSamples.R4.create().diagnosticReport("x");
    assertThat(controller().searchById("x", 1, 15))
        .isEqualTo(
            DiagnosticReportSamples.R4.asBundle(
                "http://fonzy.com/cool",
                List.of(r4),
                1,
                link(
                    BundleLink.LinkRelation.first,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                    1,
                    15),
                link(
                    BundleLink.LinkRelation.self,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                    1,
                    15),
                link(
                    BundleLink.LinkRelation.last,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                    1,
                    15)));
  }

  @Test
  void searchByIdentifier() {
    DatamartDiagnosticReport dm = DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
    repository.save(entity(dm));
    mockDiagnosticReportEntity("x", dm.cdwId());
    DiagnosticReport r4 = DiagnosticReportSamples.R4.create().diagnosticReport("x");
    assertThat(controller().searchByIdentifier("x", 1, 15))
        .isEqualTo(
            DiagnosticReportSamples.R4.asBundle(
                "http://fonzy.com/cool",
                List.of(r4),
                1,
                link(
                    BundleLink.LinkRelation.first,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                    1,
                    15),
                link(
                    BundleLink.LinkRelation.self,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                    1,
                    15),
                link(
                    BundleLink.LinkRelation.last,
                    "http://fonzy.com/cool/DiagnosticReport?identifier=x",
                    1,
                    15)));
  }

  @Test
  void searchByIdentifierCount0() {
    DatamartDiagnosticReport datamart =
        DiagnosticReportSamples.DatamartV2.create().diagnosticReport();
    repository.save(entity(datamart));
    assertThat(json(controller().searchByIdentifier("800260864479:L", 1, 0)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    emptyList(),
                    1,
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?identifier="
                            + encode("800260864479:L"),
                        1,
                        0))));
  }

  @Test
  void searchByPatient() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports = diagnosticReportsByPatient.get("p0");
    assertThat(json(controller().searchByPatient("p0", 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCategoryAndNoDate() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(dr -> "LAB".equals(dr.category().get(0).coding().get(0).code()))
            .collect(Collectors.toList());
    assertThat(json(controller().searchByPatientAndCategory("p0", "LAB", null, 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?category=LAB&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?category=LAB&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?category=LAB&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCategoryAndOneDate() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(dr -> "LAB".equals(dr.category().get(0).coding().get(0).code()))
            .filter(dr -> "2020-01-20T07:57:00Z".equals(dr.issued()))
            .collect(Collectors.toList());
    String encoded = encode("eq2020-01-20T07:57:00Z");
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(
                        "p0", "LAB", new String[] {"eq2020-01-20T07:57:00Z"}, 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date="
                            + encoded
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date="
                            + encoded
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date="
                            + encoded
                            + "&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCategoryAndTwoDates() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(dr -> "LAB".equals(dr.category().get(0).coding().get(0).code()))
            .filter(
                dr -> {
                  Instant issued = Instant.parse(dr.issued());
                  return issued.isAfter(Instant.parse("2020-01-20T07:57:00Z"))
                      && issued.isBefore(Instant.parse("2020-01-26T07:57:00Z"));
                })
            .collect(Collectors.toList());
    String encodedDate1 = encode("gt2020-01-20T07:57:00Z");
    String encodedDate2 = encode("le2020-01-25T07:57:00Z");
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(
                        "p0",
                        "LAB",
                        new String[] {"gt2020-01-20T07:57:00Z", "le2020-01-25T07:57:00Z"},
                        1,
                        15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date="
                            + encodedDate1
                            + "&date="
                            + encodedDate2
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date="
                            + encodedDate1
                            + "&date="
                            + encodedDate2
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?category=LAB"
                            + "&date="
                            + encodedDate1
                            + "&date="
                            + encodedDate2
                            + "&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCategoryTokenSearches() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports = diagnosticReportsByPatient.get("p0");
    // Unknown System, Any Code
    assertThat(json(controller().searchByPatientAndCategory("p0", "http://big.oof|", null, 1, 15)))
        .isEqualTo(json(emptyBundleOf("category=" + encode("http://big.oof|") + "&patient=p0")));
    // Unknown System, Explicit Code
    assertThat(
            json(controller().searchByPatientAndCategory("p0", "http://big.oof|LAB", null, 1, 15)))
        .isEqualTo(json(emptyBundleOf("category=" + encode("http://big.oof|LAB") + "&patient=p0")));
    // Explicit System, Unknown Code
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(
                        "p0", "http://terminology.hl7.org/CodeSystem/v2-0074|NOPE", null, 1, 15)))
        .isEqualTo(
            json(
                emptyBundleOf(
                    "category="
                        + encode("http://terminology.hl7.org/CodeSystem/v2-0074|NOPE")
                        + "&patient=p0")));
    // No System, Unknown Code
    assertThat(json(controller().searchByPatientAndCategory("p0", "|NOPE", null, 1, 15)))
        .isEqualTo(json(emptyBundleOf("category=" + encode("|NOPE") + "&patient=p0")));
    // No System, Explicit Code
    assertThat(json(controller().searchByPatientAndCategory("p0", "|LAB", null, 1, 15)))
        .isEqualTo(json(emptyBundleOf("category=" + encode("|LAB") + "&patient=p0")));
    // Explicit System, Explicit Code
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(
                        "p0", "http://terminology.hl7.org/CodeSystem/v2-0074|LAB", null, 1, 15)))
        .isEqualTo(
            json(
                bundleOf(
                    diagnosticReports,
                    "category="
                        + encode("http://terminology.hl7.org/CodeSystem/v2-0074|LAB")
                        + "&patient=p0",
                    1,
                    15)));
    // Explicit System, Any Code
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(
                        "p0", "http://terminology.hl7.org/CodeSystem/v2-0074|", null, 1, 15)))
        .isEqualTo(
            json(
                bundleOf(
                    diagnosticReports,
                    "category="
                        + encode("http://terminology.hl7.org/CodeSystem/v2-0074|")
                        + "&patient=p0",
                    1,
                    15)));
  }

  @Test
  void searchByPatientAndCategoryWithUnknownCode() {
    populateData();
    assertThat(json(controller().searchByPatientAndCategory("p0", "NOPE", null, 1, 15)))
        .isEqualTo(json(emptyBundleOf("category=NOPE&patient=p0")));
  }

  @Test
  void searchByPatientAndCodeAndNoDate() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports = diagnosticReportsByPatient.get("p0");
    assertThat(json(controller().searchByPatientAndCode("p0", "panel", null, 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?code=panel&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?code=panel&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?code=panel&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCodeAndOneDate() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(dr -> "2020-01-20T07:57:00Z".equals(dr.issued()))
            .collect(Collectors.toList());
    String encoded = encode("eq2020-01-20T07:57:00Z");
    assertThat(
            json(
                controller()
                    .searchByPatientAndCode(
                        "p0", "", new String[] {"eq2020-01-20T07:57:00Z"}, 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?code=&date="
                            + encoded
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?code=&date="
                            + encoded
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?code=&date="
                            + encoded
                            + "&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCodeAndTwoDates() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(
                dr -> {
                  Instant issued = Instant.parse(dr.issued());
                  return issued.isAfter(Instant.parse("2020-01-20T07:57:00Z"))
                      && issued.isBefore(Instant.parse("2020-01-26T07:57:00Z"));
                })
            .collect(Collectors.toList());
    assertThat(
            json(
                controller()
                    .searchByPatientAndCode(
                        "p0",
                        "",
                        new String[] {"gt2020-01-20T07:57:00Z", "le2020-01-25T07:57:00Z"},
                        1,
                        15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?code="
                            + "&date="
                            + encode("gt2020-01-20T07:57:00Z")
                            + "&date="
                            + encode("le2020-01-25T07:57:00Z")
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?code="
                            + "&date="
                            + encode("gt2020-01-20T07:57:00Z")
                            + "&date="
                            + encode("le2020-01-25T07:57:00Z")
                            + "&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport"
                            + "?code="
                            + "&date="
                            + encode("gt2020-01-20T07:57:00Z")
                            + "&date="
                            + encode("le2020-01-25T07:57:00Z")
                            + "&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndCodeTokenSearches() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports = diagnosticReportsByPatient.get("p0");
    // Unknown System, Any Code
    assertThat(json(controller().searchByPatientAndCode("p0", "http://big.oof|", null, 1, 15)))
        .isEqualTo(json(emptyBundleOf("code=" + encode("http://big.oof|") + "&patient=p0")));
    // Unknown System, Explicit Code
    assertThat(json(controller().searchByPatientAndCode("p0", "http://big.oof|panel", null, 1, 15)))
        .isEqualTo(json(emptyBundleOf("code=" + encode("http://big.oof|panel") + "&patient=p0")));
    // No System, Unknown Code
    assertThat(json(controller().searchByPatientAndCode("p0", "|NOPE", null, 1, 15)))
        .isEqualTo(json(emptyBundleOf("code=" + encode("|NOPE") + "&patient=p0")));
    // No System, Explicit Code
    assertThat(json(controller().searchByPatientAndCode("p0", "|panel", null, 1, 15)))
        .isEqualTo(json(emptyBundleOf("code=" + encode("|panel") + "&patient=p0")));
  }

  @Test
  void searchByPatientAndEmptyCodeAndNoDate() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports = diagnosticReportsByPatient.get("p0");
    assertThat(json(controller().searchByPatientAndCode("p0", "", null, 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?code=&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?code=&patient=p0",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?code=&patient=p0",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndStatus() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(dr -> DiagnosticReport.DiagnosticReportStatus._final.equals(dr.status()))
            .collect(Collectors.toList());
    assertThat(json(controller().searchByPatientAndStatus("p0", "final", 1, 15)))
        .isEqualTo(
            json(
                DiagnosticReportSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    diagnosticReports,
                    diagnosticReports.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/DiagnosticReport?patient=p0&status=final",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/DiagnosticReport?patient=p0&status=final",
                        1,
                        15),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/DiagnosticReport?patient=p0&status=final",
                        1,
                        15))));
  }

  @Test
  void searchByPatientAndStatusTokenSearches() {
    Multimap<String, DiagnosticReport> diagnosticReportsByPatient = populateData();
    Collection<DiagnosticReport> diagnosticReports =
        diagnosticReportsByPatient.get("p0").stream()
            .filter(dr -> DiagnosticReport.DiagnosticReportStatus._final.equals(dr.status()))
            .collect(Collectors.toList());
    // Unknown System, Any Code
    assertThat(json(controller().searchByPatientAndStatus("p0", "http://big.oof|", 1, 15)))
        .isEqualTo(json(emptyBundleOf("patient=p0&status=" + encode("http://big.oof|"))));
    // Unknown System, Explicit Code
    assertThat(json(controller().searchByPatientAndStatus("p0", "http://big.oof|final", 1, 15)))
        .isEqualTo(json(emptyBundleOf("patient=p0&status=" + encode("http://big.oof|final"))));
    // Explicit System, Unknown Code
    assertThat(
            json(
                controller()
                    .searchByPatientAndStatus(
                        "p0", "http://hl7.org/fhir/diagnostic-report-status|NOPE", 1, 15)))
        .isEqualTo(
            json(
                emptyBundleOf(
                    "patient=p0&status="
                        + encode("http://hl7.org/fhir/diagnostic-report-status|NOPE"))));
    // No System, Unknown Code
    assertThat(json(controller().searchByPatientAndStatus("p0", "|NOPE", 1, 15)))
        .isEqualTo(json(emptyBundleOf("patient=p0&status=" + encode("|NOPE"))));
    // No System, Explicit Code
    assertThat(json(controller().searchByPatientAndStatus("p0", "|final", 1, 15)))
        .isEqualTo(json(emptyBundleOf("patient=p0&status=" + encode("|final"))));
    // Explicit System, Explicit Code
    assertThat(
            json(
                controller()
                    .searchByPatientAndStatus(
                        "p0", "http://hl7.org/fhir/diagnostic-report-status|final", 1, 15)))
        .isEqualTo(
            json(
                bundleOf(
                    diagnosticReports,
                    "patient=p0&status="
                        + encode("http://hl7.org/fhir/diagnostic-report-status|final"),
                    1,
                    15)));
    // Explicit System, Any Code
    assertThat(
            json(
                controller()
                    .searchByPatientAndStatus(
                        "p0", "http://hl7.org/fhir/diagnostic-report-status|", 1, 15)))
        .isEqualTo(
            json(
                bundleOf(
                    diagnosticReports,
                    "patient=p0&status=" + encode("http://hl7.org/fhir/diagnostic-report-status|"),
                    1,
                    15)));
  }

  @Test
  void searchByPatientAndUnknownStatus() {
    populateData();
    assertThat(json(controller().searchByPatientAndStatus("p0", "NOPE", 1, 15)))
        .isEqualTo(json(emptyBundleOf("patient=p0&status=NOPE")));
  }
}
