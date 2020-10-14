package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.health.dataquery.service.controller.observation.ObservationSamples.R4.link;
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
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.resources.Observation;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@DataJpaTest
@ExtendWith(SpringExtension.class)
public class R4ObservationControllerTest {
  private static final String OBSERVATION_CATEGORY_SYSTEM =
      "http://terminology.hl7.org/CodeSystem/observation-category";

  private static final String OBSERVATION_CODE_SYSTEM = "http://loinc.org";

  private final IdentityService ids = mock(IdentityService.class);

  HttpServletResponse response = mock(HttpServletResponse.class);

  @Autowired private ObservationRepository repository;

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @SneakyThrows
  private static ObservationEntity asEntity(DatamartObservation dm) {
    return ObservationEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.subject().get().reference().get())
        .category(
            dm.category().equals(DatamartObservation.Category.vital_signs)
                ? "vital-signs"
                : dm.category().toString())
        .code(dm.code().get().coding().get().code().get())
        .dateUtc(dm.effectiveDateTime().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  @SneakyThrows
  private static DatamartObservation toObject(String payload) {
    return JacksonConfig.createMapper().readValue(payload, DatamartObservation.class);
  }

  R4ObservationController controller() {
    return new R4ObservationController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockObservationIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("OBSERVATION").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  private Multimap<String, Observation> populateData() {
    var fhir = ObservationSamples.R4.create();
    var datamart = ObservationSamples.Datamart.create();
    var observationsByPatient = LinkedHashMultimap.<String, Observation>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      var patientId = "p" + i % 2;
      var cdwId = "cdw-" + i;
      var publicId = "public" + i;
      var date = "2005-01-1" + i + "T07:57:00Z";
      DatamartObservation dm = datamart.observation(cdwId, patientId);
      if (i >= 5) {
        dm.category(DatamartObservation.Category.vital_signs);
      }
      if (i % 2 == 1) {
        dm.code().get().coding().get().code(Optional.of("8480-6"));
      }
      dm.effectiveDateTime(Optional.of(Instant.parse(date)));
      repository.save(asEntity(dm));
      Observation observation = fhir.observation(publicId, patientId);
      if (i >= 5) {
        observation.category(
            List.of(
                CodeableConcept.builder()
                    .coding(
                        List.of(
                            Coding.builder()
                                .system(
                                    "http://terminology.hl7.org/CodeSystem/observation-category")
                                .code("vital-signs")
                                .display("Vital Signs")
                                .build()))
                    .build()));
      }
      if (i % 2 == 1) {
        observation.code().coding().get(0).code("8480-6");
      }
      observation.effectiveDateTime(date);
      observationsByPatient.put(patientId, observation);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder()
              .system("CDW")
              .resource("OBSERVATION")
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
    return observationsByPatient;
  }

  @Test
  void read() {
    DatamartObservation dm = ObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    mockObservationIdentity("x", dm.cdwId());
    Observation actual = controller().read("x");
    assertThat(actual).isEqualTo(ObservationSamples.R4.create().observation("x"));
  }

  @Test
  void readRaw() {
    DatamartObservation dm = ObservationSamples.Datamart.create().observation();
    ObservationEntity entity = asEntity(dm);
    repository.save(entity);
    mockObservationIdentity("x", dm.cdwId());
    String json = controller().readRaw("x", response);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test
  void readRawThrowsNotFoundWhenDataIsMissing() {
    mockObservationIdentity("x", "x");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("x", response));
  }

  @Test
  void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("x", response));
  }

  @Test
  void readThrowsNotFoundWhenDataIsMissing() {
    mockObservationIdentity("x", "x");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("x"));
  }

  @Test
  void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("x", response));
  }

  @Test
  void searchById() {
    DatamartObservation dm = ObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    mockObservationIdentity("x", dm.cdwId());
    Observation.Bundle actual = controller().searchById("x", 1, 1);
    Observation observation = ObservationSamples.R4.create().observation("x");
    assertThat(json(actual))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    List.of(observation),
                    1,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?identifier=x",
                        1,
                        1),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?identifier=x",
                        1,
                        1),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?identifier=x",
                        1,
                        1))));
    /* searchById and searchByIdentifier are the same */
    assertThat(controller().searchByIdentifier("x", 1, 1)).isEqualTo(actual);
  }

  @Test
  void searchByIdWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().searchById("x", 1, 1));
  }

  @Test
  void searchByIdWith0Count() {
    DatamartObservation dm = ObservationSamples.Datamart.create().observation();
    repository.save(asEntity(dm));
    mockObservationIdentity("x", dm.cdwId());
    Observation.Bundle actual = controller().searchById("x", 1, 0);
    assertThat(json(actual))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    List.of(),
                    1,
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?identifier=x",
                        1,
                        0))));
    /* searchById and searchByIdentifier are the same */
    assertThat(controller().searchByIdentifier("x", 1, 0)).isEqualTo(actual);
  }

  @Test
  void searchByPatient() {
    Multimap<String, Observation> observationsByPatient = populateData();
    assertThat(json(controller().searchByPatient(false, "p0", 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    observationsByPatient.get("p0"),
                    observationsByPatient.get("p0").size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?patient=p0",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCategoryAndOneDateHack() {
    /*
    This test exhaustively verifies all of the different date prefixes
    in combination with patient and category.

    Observation dates for p0
     2005-01-10T07:57:00Z -> laboratory
     2005-01-12T07:57:00Z -> laboratory
     2005-01-14T07:57:00Z -> laboratory
     2005-01-16T07:57:00Z -> vital-signs
     2005-01-18T07:57:00Z -> vital-signs

    Observation dates for p1
     2005-01-11T07:57:00Z -> laboratory
     2005-01-13T07:57:00Z -> laboratory
     2005-01-15T07:57:00Z -> vital-signs
     2005-01-17T07:57:00Z -> vital-signs
     2005-01-19T07:57:00Z -> vital-signs
    */
    Multimap<String, Observation> observationsByPatient = populateData();
    /*
    <criteria, expected dates>
    key: search criteria (prefix + date)
    value: expected date responses
     */
    Multimap<String, String> testDates = LinkedHashMultimap.create();
    testDates.putAll(
        "gt2004", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z", "2005-01-14T07:57:00Z"));
    testDates.putAll("eq2005-01-14", List.of("2005-01-14T07:57:00Z"));
    testDates.putAll("ne2005-01-14", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z"));
    testDates.putAll(
        "le2005-01-14",
        List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z", "2005-01-14T07:57:00Z"));
    testDates.putAll("lt2005-01-14", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z"));
    testDates.putAll("eb2005-01-14", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z"));
    testDates.putAll("ge2005-01-14", List.of("2005-01-14T07:57:00Z"));
    testDates.putAll("gt2005-01-14", List.of());
    testDates.putAll("sa2005-01-14", List.of());
    for (var date : testDates.keySet()) {
      List<Observation> observations =
          observationsByPatient.get("p0").stream()
              .filter(o -> testDates.get(date).contains(o.effectiveDateTime()))
              .collect(Collectors.toList());
      assertThat(
              json(
                  controller()
                      .searchByPatientAndCategory(
                          true, "p0", "laboratory", new String[] {date}, 1, 10)))
          .isEqualTo(
              json(
                  ObservationSamples.R4.asBundle(
                      "http://fonzy.com/cool",
                      observations,
                      observations.size(),
                      link(
                          BundleLink.LinkRelation.first,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.self,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.last,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date
                              + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  void searchByPatientAndCategoryAndOneDateNoHack() {
    /*
    This test exhaustively verifies all of the different date prefixes
    in combination with patient and category.

    Observation dates for p0
     2005-01-10T07:57:00Z -> laboratory
     2005-01-12T07:57:00Z -> laboratory
     2005-01-14T07:57:00Z -> laboratory
     2005-01-16T07:57:00Z -> vital-signs
     2005-01-18T07:57:00Z -> vital-signs

    Observation dates for p1
     2005-01-11T07:57:00Z -> laboratory
     2005-01-13T07:57:00Z -> laboratory
     2005-01-15T07:57:00Z -> vital-signs
     2005-01-17T07:57:00Z -> vital-signs
     2005-01-19T07:57:00Z -> vital-signs
    */
    Multimap<String, Observation> observationsByPatient = populateData();
    /*
    <criteria, expected dates>
    key: search criteria (prefix + date)
    value: expected date responses
     */
    Multimap<String, String> testDates = LinkedHashMultimap.create();
    testDates.putAll(
        "gt2004", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z", "2005-01-14T07:57:00Z"));
    testDates.putAll("eq2005-01-14", List.of("2005-01-14T07:57:00Z"));
    testDates.putAll("ne2005-01-14", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z"));
    testDates.putAll(
        "le2005-01-14",
        List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z", "2005-01-14T07:57:00Z"));
    testDates.putAll("lt2005-01-14", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z"));
    testDates.putAll("eb2005-01-14", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z"));
    testDates.putAll("ge2005-01-14", List.of("2005-01-14T07:57:00Z"));
    testDates.putAll("gt2005-01-14", List.of());
    testDates.putAll("sa2005-01-14", List.of());
    for (var date : testDates.keySet()) {
      List<Observation> observations =
          observationsByPatient.get("p0").stream()
              .filter(o -> testDates.get(date).contains(o.effectiveDateTime()))
              .collect(Collectors.toList());
      assertThat(
              json(
                  controller()
                      .searchByPatientAndCategory(
                          false, "p0", "laboratory", new String[] {date}, 1, 10)))
          .isEqualTo(
              json(
                  ObservationSamples.R4.asBundle(
                      "http://fonzy.com/cool",
                      observations,
                      observations.size(),
                      link(
                          BundleLink.LinkRelation.first,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.self,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.last,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date
                              + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  void searchByPatientAndCategoryAndTwoDatesHack() {
    /*
    The single date test does exhaustive date-prefix searching. We won't do that here.

    Observation dates for p0
     2005-01-10T07:57:00Z -> laboratory
     2005-01-12T07:57:00Z -> laboratory
     2005-01-14T07:57:00Z -> laboratory
     2005-01-16T07:57:00Z -> vital-signs
     2005-01-18T07:57:00Z -> vital-signs

    Observation dates for p1
     2005-01-11T07:57:00Z -> laboratory
     2005-01-13T07:57:00Z -> laboratory
     2005-01-15T07:57:00Z -> vital-signs
     2005-01-17T07:57:00Z -> vital-signs
     2005-01-19T07:57:00Z -> vital-signs
     */
    Multimap<String, Observation> observationsByPatient = populateData();
    /*
    <criteria, expected dates>
    key: search criteria (prefix + date)
    value: expected date responses
     */
    Multimap<Pair<String, String>, String> testDates = LinkedHashMultimap.create();
    testDates.putAll(
        Pair.of("gt2004", "lt2006"),
        List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z", "2005-01-14T07:57:00Z"));
    testDates.putAll(Pair.of("gt2005-01-13", "lt2005-01-15"), List.of("2005-01-14T07:57:00Z"));
    for (var date : testDates.keySet()) {
      List<Observation> observations =
          observationsByPatient.get("p0").stream()
              .filter(o -> testDates.get(date).contains(o.effectiveDateTime()))
              .collect(Collectors.toList());
      assertThat(
              json(
                  controller()
                      .searchByPatientAndCategory(
                          true,
                          "p0",
                          "laboratory",
                          new String[] {date.getLeft(), date.getRight()},
                          1,
                          10)))
          .isEqualTo(
              json(
                  ObservationSamples.R4.asBundle(
                      "http://fonzy.com/cool",
                      observations,
                      observations.size(),
                      link(
                          BundleLink.LinkRelation.first,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.self,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.last,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  void searchByPatientAndCategoryAndTwoDatesNoHack() {
    /*
    The single date test does exhaustive date-prefix searching. We won't do that here.

    Observation dates for p0
     2005-01-10T07:57:00Z -> laboratory
     2005-01-12T07:57:00Z -> laboratory
     2005-01-14T07:57:00Z -> laboratory
     2005-01-16T07:57:00Z -> vital-signs
     2005-01-18T07:57:00Z -> vital-signs

    Observation dates for p1
     2005-01-11T07:57:00Z -> laboratory
     2005-01-13T07:57:00Z -> laboratory
     2005-01-15T07:57:00Z -> vital-signs
     2005-01-17T07:57:00Z -> vital-signs
     2005-01-19T07:57:00Z -> vital-signs
     */
    Multimap<String, Observation> observationsByPatient = populateData();
    /*
    <criteria, expected dates>
    key: search criteria (prefix + date)
    value: expected date responses
     */
    Multimap<Pair<String, String>, String> testDates = LinkedHashMultimap.create();
    testDates.putAll(
        Pair.of("gt2004", "lt2006"),
        List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z", "2005-01-14T07:57:00Z"));
    testDates.putAll(Pair.of("gt2005-01-13", "lt2005-01-15"), List.of("2005-01-14T07:57:00Z"));
    for (var date : testDates.keySet()) {
      List<Observation> observations =
          observationsByPatient.get("p0").stream()
              .filter(o -> testDates.get(date).contains(o.effectiveDateTime()))
              .collect(Collectors.toList());
      assertThat(
              json(
                  controller()
                      .searchByPatientAndCategory(
                          false,
                          "p0",
                          "laboratory",
                          new String[] {date.getLeft(), date.getRight()},
                          1,
                          10)))
          .isEqualTo(
              json(
                  ObservationSamples.R4.asBundle(
                      "http://fonzy.com/cool",
                      observations,
                      observations.size(),
                      link(
                          BundleLink.LinkRelation.first,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.self,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.last,
                          "http://fonzy.com/cool/Observation?category=laboratory&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  void searchByPatientAndCategoryHack() {
    Multimap<String, Observation> observationsByPatient = populateData();
    List<Observation> observations =
        observationsByPatient.get("p0").stream()
            .filter(c -> "laboratory".equalsIgnoreCase(c.category().get(0).coding().get(0).code()))
            .collect(Collectors.toList());
    assertThat(json(controller().searchByPatientAndCategory(true, "p0", "laboratory", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    observations,
                    observations.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?category=laboratory&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?category=laboratory&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?category=laboratory&patient=p0",
                        1,
                        10))));
    List<Observation> patient1Observations =
        observationsByPatient.get("p1").stream()
            .filter(c -> "vital-signs".equalsIgnoreCase(c.category().get(0).coding().get(0).code()))
            .collect(Collectors.toList());
    assertThat(
            json(controller().searchByPatientAndCategory(true, "p1", "vital-signs", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    patient1Observations,
                    patient1Observations.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?category=vital-signs&patient=p1",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?category=vital-signs&patient=p1",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?category=vital-signs&patient=p1",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCategoryNoHack() {
    Multimap<String, Observation> observationsByPatient = populateData();
    List<Observation> observations =
        observationsByPatient.get("p0").stream()
            .filter(c -> "laboratory".equalsIgnoreCase(c.category().get(0).coding().get(0).code()))
            .collect(Collectors.toList());
    assertThat(
            json(controller().searchByPatientAndCategory(false, "p0", "laboratory", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    observations,
                    observations.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?category=laboratory&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?category=laboratory&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?category=laboratory&patient=p0",
                        1,
                        10))));
    List<Observation> patient1Observations =
        observationsByPatient.get("p1").stream()
            .filter(c -> "vital-signs".equalsIgnoreCase(c.category().get(0).coding().get(0).code()))
            .collect(Collectors.toList());
    assertThat(
            json(controller().searchByPatientAndCategory(false, "p1", "vital-signs", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    patient1Observations,
                    patient1Observations.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?category=vital-signs&patient=p1",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?category=vital-signs&patient=p1",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?category=vital-signs&patient=p1",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCategorySystem() {
    Multimap<String, Observation> observationsByPatient = populateData();
    String encoded = encode(OBSERVATION_CATEGORY_SYSTEM + "|");
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(
                        false, "p0", OBSERVATION_CATEGORY_SYSTEM + "|", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    observationsByPatient.get("p0"),
                    observationsByPatient.get("p0").size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCategorySystemAndCode() {
    Multimap<String, Observation> observationsByPatient = populateData();
    List<Observation> labs =
        observationsByPatient.get("p0").stream()
            .filter(o -> "laboratory".equals(o.category().get(0).coding().get(0).code()))
            .collect(Collectors.toList());
    String encoded = encode(OBSERVATION_CATEGORY_SYSTEM + "|laboratory");
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(
                        false, "p0", OBSERVATION_CATEGORY_SYSTEM + "|laboratory", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    labs,
                    labs.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCategorySystemUnknown() {
    Multimap<String, Observation> observationsByPatient = populateData();
    String encoded = encode("http://nope.com|");
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(false, "p0", "http://nope.com|", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    List.of(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCategoryTokenMix() {
    /*
     * We'll search categories
     * http://terminology.hl7.org/CodeSystem/observation-category|vital-signs and |laboratory. The
     * "|laboratory" will get ignored since it explicitly sets a 'no system' value.
     */
    Multimap<String, Observation> observationsByPatient = populateData();
    Collection<Observation> vitalsigns =
        observationsByPatient.get("p0").stream().skip(3).collect(Collectors.toList());
    String encoded = encode(OBSERVATION_CATEGORY_SYSTEM + "|vital-signs,|laboratory");
    assertThat(
            json(
                controller()
                    .searchByPatientAndCategory(
                        false,
                        "p0",
                        OBSERVATION_CATEGORY_SYSTEM + "|vital-signs,|laboratory",
                        null,
                        1,
                        10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    vitalsigns,
                    vitalsigns.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?category=" + encoded + "&patient=p0",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCode() {
    Multimap<String, Observation> observationsByPatient = populateData();
    List<Observation> patient0Observations =
        observationsByPatient.get("p0").stream()
            .filter(c -> "1989-3".equalsIgnoreCase(c.code().coding().get(0).code()))
            .collect(Collectors.toList());
    assertThat(json(controller().searchByPatientAndCode("p0", "1989-3", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    patient0Observations,
                    patient0Observations.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?code=1989-3&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?code=1989-3&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?code=1989-3&patient=p0",
                        1,
                        10))));
    List<Observation> patient1Observations =
        observationsByPatient.get("p1").stream()
            .filter(c -> "8480-6".equalsIgnoreCase(c.code().coding().get(0).code()))
            .collect(Collectors.toList());
    assertThat(json(controller().searchByPatientAndCode("p1", "8480-6", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    patient1Observations,
                    patient1Observations.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?code=8480-6&patient=p1",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?code=8480-6&patient=p1",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?code=8480-6&patient=p1",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCodeAndOneDate() {
    /* Codes end up getting populated on a patient to patient basis,
     * That is to say p0 is always 1989-3 and p1 is always 8480-6 */
    Multimap<String, Observation> observationsByPatient = populateData();
    Multimap<String, String> testDates = LinkedHashMultimap.create();
    testDates.putAll(
        "gt2004",
        List.of(
            "2005-01-10T07:57:00Z",
            "2005-01-12T07:57:00Z",
            "2005-01-14T07:57:00Z",
            "2005-01-16T07:57:00Z",
            "2005-01-18T07:57:00Z"));
    testDates.putAll("eq2005-01-14", List.of("2005-01-14T07:57:00Z"));
    testDates.putAll(
        "ne2005-01-14",
        List.of(
            "2005-01-10T07:57:00Z",
            "2005-01-12T07:57:00Z",
            "2005-01-16T07:57:00Z",
            "2005-01-18T07:57:00Z"));
    testDates.putAll(
        "le2005-01-14",
        List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z", "2005-01-14T07:57:00Z"));
    testDates.putAll("lt2005-01-14", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z"));
    testDates.putAll("eb2005-01-14", List.of("2005-01-10T07:57:00Z", "2005-01-12T07:57:00Z"));
    testDates.putAll(
        "ge2005-01-14",
        List.of("2005-01-14T07:57:00Z", "2005-01-16T07:57:00Z", "2005-01-18T07:57:00Z"));
    testDates.putAll("gt2005-01-18", List.of());
    testDates.putAll("sa2005-01-18", List.of());
    for (var date : testDates.keySet()) {
      List<Observation> observations =
          observationsByPatient.get("p0").stream()
              .filter(o -> testDates.get(date).contains(o.effectiveDateTime()))
              .collect(Collectors.toList());
      assertThat(
              json(controller().searchByPatientAndCode("p0", "1989-3", new String[] {date}, 1, 10)))
          .isEqualTo(
              json(
                  ObservationSamples.R4.asBundle(
                      "http://fonzy.com/cool",
                      observations,
                      observations.size(),
                      link(
                          BundleLink.LinkRelation.first,
                          "http://fonzy.com/cool/Observation?code=1989-3&date="
                              + date
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.self,
                          "http://fonzy.com/cool/Observation?code=1989-3&date="
                              + date
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.last,
                          "http://fonzy.com/cool/Observation?code=1989-3&date="
                              + date
                              + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  void searchByPatientAndCodeAndTwoDates() {
    Multimap<String, Observation> observationsByPatient = populateData();
    Multimap<Pair<String, String>, String> testDates = LinkedHashMultimap.create();
    testDates.putAll(
        Pair.of("gt2004", "lt2006"),
        List.of(
            "2005-01-10T07:57:00Z",
            "2005-01-12T07:57:00Z",
            "2005-01-14T07:57:00Z",
            "2005-01-16T07:57:00Z",
            "2005-01-18T07:57:00Z"));
    testDates.putAll(
        Pair.of("ge2005-01-12", "le2005-01-14"),
        List.of("2005-01-12T07:57:00Z", "2005-01-14T07:57:00Z"));
    for (var date : testDates.keySet()) {
      List<Observation> observations =
          observationsByPatient.get("p0").stream()
              .filter(o -> testDates.get(date).contains(o.effectiveDateTime()))
              .collect(Collectors.toList());
      assertThat(
              json(
                  controller()
                      .searchByPatientAndCode(
                          "p0", "1989-3", new String[] {date.getLeft(), date.getRight()}, 1, 10)))
          .isEqualTo(
              json(
                  ObservationSamples.R4.asBundle(
                      "http://fonzy.com/cool",
                      observations,
                      observations.size(),
                      link(
                          BundleLink.LinkRelation.first,
                          "http://fonzy.com/cool/Observation?code=1989-3&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.self,
                          "http://fonzy.com/cool/Observation?code=1989-3&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.last,
                          "http://fonzy.com/cool/Observation?code=1989-3&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  void searchByPatientAndCodeSystem() {
    Multimap<String, Observation> observationsByPatient = populateData();
    String encoded = encode(OBSERVATION_CODE_SYSTEM + "|");
    assertThat(
            json(
                controller()
                    .searchByPatientAndCode("p0", OBSERVATION_CODE_SYSTEM + "|", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    observationsByPatient.get("p0"),
                    observationsByPatient.get("p0").size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCodeSystemAndCode() {
    Multimap<String, Observation> observationsByPatient = populateData();
    List<Observation> patient0Observations =
        observationsByPatient.get("p0").stream()
            .filter(c -> "1989-3".equalsIgnoreCase(c.code().coding().get(0).code()))
            .collect(Collectors.toList());
    String encoded = encode(OBSERVATION_CODE_SYSTEM + "|1989-3");
    assertThat(
            json(
                controller()
                    .searchByPatientAndCode(
                        "p0", OBSERVATION_CODE_SYSTEM + "|1989-3", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    patient0Observations,
                    patient0Observations.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCodeSystemUnknown() {
    Multimap<String, Observation> observationsByPatient = populateData();
    String encoded = encode("http://nope.com|");
    assertThat(json(controller().searchByPatientAndCode("p0", "http://nope.com|", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    List.of(),
                    0,
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndCodeTokenMix() {
    Multimap<String, Observation> observationsByPatient = populateData();
    List<Observation> patient0Observations =
        observationsByPatient.get("p0").stream()
            .filter(c -> "1989-3".equalsIgnoreCase(c.code().coding().get(0).code()))
            .collect(Collectors.toList());
    String encoded = encode(OBSERVATION_CODE_SYSTEM + "|1989-3,|8480-6");
    assertThat(
            json(
                controller()
                    .searchByPatientAndCode(
                        "p0", OBSERVATION_CODE_SYSTEM + "|1989-3,|8480-6", null, 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    patient0Observations,
                    patient0Observations.size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?code=" + encoded + "&patient=p0",
                        1,
                        10))));
  }

  @Test
  void searchByPatientAndTwoCategoriesAndTwoDatesHack() {
    /*
    Observation dates for p0
     2005-01-10T07:57:00Z -> laboratory
     2005-01-12T07:57:00Z -> laboratory
     2005-01-14T07:57:00Z -> laboratory
     2005-01-16T07:57:00Z -> vital-signs
     2005-01-18T07:57:00Z -> vital-signs

    Observation dates for p1
     2005-01-11T07:57:00Z -> laboratory
     2005-01-13T07:57:00Z -> laboratory
     2005-01-15T07:57:00Z -> vital-signs
     2005-01-17T07:57:00Z -> vital-signs
     2005-01-19T07:57:00Z -> vital-signs
    */
    Multimap<String, Observation> observationsByPatient = populateData();
    /*
    <criteria, expected dates>
    key: search criteria (prefix + date)
    value: expected date responses
     */
    Multimap<Pair<String, String>, String> testDates = LinkedHashMultimap.create();
    testDates.putAll(
        Pair.of("gt2004", "lt2006"),
        List.of(
            "2005-01-10T07:57:00Z",
            "2005-01-12T07:57:00Z",
            "2005-01-14T07:57:00Z",
            "2005-01-16T07:57:00Z",
            "2005-01-18T07:57:00Z"));
    testDates.putAll(
        Pair.of("gt2005-01-13", "lt2005-01-17"),
        List.of("2005-01-14T07:57:00Z", "2005-01-16T07:57:00Z"));
    String encodedCat = encode("laboratory,vital-signs");
    for (var date : testDates.keySet()) {
      List<Observation> observations =
          observationsByPatient.get("p0").stream()
              .filter(o -> testDates.get(date).contains(o.effectiveDateTime()))
              .collect(Collectors.toList());
      assertThat(
              json(
                  controller()
                      .searchByPatientAndCategory(
                          true,
                          "p0",
                          "laboratory,vital-signs",
                          new String[] {date.getLeft(), date.getRight()},
                          1,
                          10)))
          .isEqualTo(
              json(
                  ObservationSamples.R4.asBundle(
                      "http://fonzy.com/cool",
                      observations,
                      observations.size(),
                      link(
                          BundleLink.LinkRelation.first,
                          "http://fonzy.com/cool/Observation?category="
                              + encodedCat
                              + "&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.self,
                          "http://fonzy.com/cool/Observation?category="
                              + encodedCat
                              + "&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.last,
                          "http://fonzy.com/cool/Observation?category="
                              + encodedCat
                              + "&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  void searchByPatientAndTwoCategoriesAndTwoDatesNoHack() {
    /*
    Observation dates for p0
     2005-01-10T07:57:00Z -> laboratory
     2005-01-12T07:57:00Z -> laboratory
     2005-01-14T07:57:00Z -> laboratory
     2005-01-16T07:57:00Z -> vital-signs
     2005-01-18T07:57:00Z -> vital-signs

    Observation dates for p1
     2005-01-11T07:57:00Z -> laboratory
     2005-01-13T07:57:00Z -> laboratory
     2005-01-15T07:57:00Z -> vital-signs
     2005-01-17T07:57:00Z -> vital-signs
     2005-01-19T07:57:00Z -> vital-signs
    */
    Multimap<String, Observation> observationsByPatient = populateData();
    /*
    <criteria, expected dates>
    key: search criteria (prefix + date)
    value: expected date responses
     */
    Multimap<Pair<String, String>, String> testDates = LinkedHashMultimap.create();
    testDates.putAll(
        Pair.of("gt2004", "lt2006"),
        List.of(
            "2005-01-10T07:57:00Z",
            "2005-01-12T07:57:00Z",
            "2005-01-14T07:57:00Z",
            "2005-01-16T07:57:00Z",
            "2005-01-18T07:57:00Z"));
    testDates.putAll(
        Pair.of("gt2005-01-13", "lt2005-01-17"),
        List.of("2005-01-14T07:57:00Z", "2005-01-16T07:57:00Z"));
    for (var date : testDates.keySet()) {
      List<Observation> observations =
          observationsByPatient.get("p0").stream()
              .filter(o -> testDates.get(date).contains(o.effectiveDateTime()))
              .collect(Collectors.toList());
      String encoded = encode("laboratory,vital-signs");
      assertThat(
              json(
                  controller()
                      .searchByPatientAndCategory(
                          false,
                          "p0",
                          "laboratory,vital-signs",
                          new String[] {date.getLeft(), date.getRight()},
                          1,
                          10)))
          .isEqualTo(
              json(
                  ObservationSamples.R4.asBundle(
                      "http://fonzy.com/cool",
                      observations,
                      observations.size(),
                      link(
                          BundleLink.LinkRelation.first,
                          "http://fonzy.com/cool/Observation?category="
                              + encoded
                              + "&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.self,
                          "http://fonzy.com/cool/Observation?category="
                              + encoded
                              + "&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10),
                      link(
                          BundleLink.LinkRelation.last,
                          "http://fonzy.com/cool/Observation?category="
                              + encoded
                              + "&date="
                              + date.getLeft()
                              + "&date="
                              + date.getRight()
                              + "&patient=p0",
                          1,
                          10))));
    }
  }

  @Test
  void searchByPatientHack() {
    Multimap<String, Observation> observationsByPatient = populateData();
    assertThat(json(controller().searchByPatient(true, "p0", 1, 10)))
        .isEqualTo(
            json(
                ObservationSamples.R4.asBundle(
                    "http://fonzy.com/cool",
                    observationsByPatient.get("p0"),
                    observationsByPatient.get("p0").size(),
                    link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Observation?patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Observation?patient=p0",
                        1,
                        10),
                    link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Observation?patient=p0",
                        1,
                        10))));
  }
}
