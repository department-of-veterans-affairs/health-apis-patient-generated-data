package gov.va.api.health.dataquery.service.controller.immunization;

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
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.immunization.ImmunizationSamples.R4;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Immunization;
import gov.va.api.health.r4.api.resources.Immunization.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public class R4ImmunizationControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private ImmunizationRepository repository;

  @SneakyThrows
  private ImmunizationEntity asEntity(DatamartImmunization dm) {
    return ImmunizationEntity.builder()
        .cdwId(dm.cdwId())
        .icn(dm.patient().reference().get())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  R4ImmunizationController controller() {
    return new R4ImmunizationController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockImmunizationIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("IMMUNIZATION").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  private Multimap<String, Immunization> populateData() {
    var fhir = R4.create();
    var datamart = Datamart.create();
    var immunizationByPatient = LinkedHashMultimap.<String, Immunization>create();
    var registrations = new ArrayList<Registration>(10);
    for (int i = 0; i < 10; i++) {
      String patientId = "p" + i % 2;
      String cdwId = "" + i;
      String publicId = "90" + i;
      var dm = datamart.immunization(cdwId, patientId);
      repository.save(asEntity(dm));
      var immunization = fhir.immunization(publicId, patientId);
      immunizationByPatient.put(patientId, immunization);
      ResourceIdentity resourceIdentity =
          ResourceIdentity.builder()
              .system("CDW")
              .resource("IMMUNIZATION")
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
    return immunizationByPatient;
  }

  @Test
  public void read() {
    DatamartImmunization dm = Datamart.create().immunization();
    repository.save(asEntity(dm));
    mockImmunizationIdentity("1", dm.cdwId());
    Immunization actual = controller().read("1");
    assertThat(json(actual))
        .isEqualTo(json(R4.create().immunization("1", dm.patient().reference().get())));
  }

  @Test
  public void readRaw() {
    DatamartImmunization dm = Datamart.create().immunization();
    ImmunizationEntity entity = asEntity(dm);
    repository.save(entity);
    mockImmunizationIdentity("1", dm.cdwId());
    String json = controller().readRaw("1", response);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", entity.icn());
  }

  @Test
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockImmunizationIdentity("1", "1");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("1", response));
  }

  @Test
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("1", response));
  }

  @Test
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockImmunizationIdentity("1", "1");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("1"));
  }

  @Test
  public void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("1"));
  }

  @Test
  public void searchById() {
    DatamartImmunization dm = Datamart.create().immunization();
    repository.save(asEntity(dm));
    mockImmunizationIdentity("1", dm.cdwId());
    Bundle actual = controller().searchById("1", 1, 1);
    Immunization immunization = R4.create().immunization("1", dm.patient().reference().get());
    assertThat(json(actual))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://fonzy.com/cool",
                    List.of(immunization),
                    1,
                    R4.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Immunization?identifier=1",
                        1,
                        1),
                    R4.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Immunization?identifier=1",
                        1,
                        1),
                    R4.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Immunization?identifier=1",
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartImmunization dm = Datamart.create().immunization();
    repository.save(asEntity(dm));
    mockImmunizationIdentity("1", dm.cdwId());
    assertThat(json(controller().searchByIdentifier("1", 1, 0)))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    1,
                    R4.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Immunization?identifier=1",
                        1,
                        0))));
  }

  @Test
  public void searchByPatient() {
    Multimap<String, Immunization> immunizationByPatient = populateData();
    assertThat(json(controller().searchByPatient("p0", 1, 10)))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://fonzy.com/cool",
                    immunizationByPatient.get("p0"),
                    immunizationByPatient.get("p0").size(),
                    R4.link(
                        BundleLink.LinkRelation.first,
                        "http://fonzy.com/cool/Immunization?patient=p0",
                        1,
                        10),
                    R4.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Immunization?patient=p0",
                        1,
                        10),
                    R4.link(
                        BundleLink.LinkRelation.last,
                        "http://fonzy.com/cool/Immunization?patient=p0",
                        1,
                        10))));
  }

  @Test
  public void searchByPatientWithCount0() {
    Multimap<String, Immunization> immunizationByPatient = populateData();
    assertThat(json(controller().searchByPatient("p0", 1, 0)))
        .isEqualTo(
            json(
                R4.asBundle(
                    "http://fonzy.com/cool",
                    Collections.emptyList(),
                    immunizationByPatient.get("p0").size(),
                    R4.link(
                        BundleLink.LinkRelation.self,
                        "http://fonzy.com/cool/Immunization?patient=p0",
                        1,
                        0))));
  }

  @SneakyThrows
  private DatamartImmunization toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartImmunization.class);
  }
}
