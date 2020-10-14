package gov.va.api.health.dataquery.service.controller.medication;

import static gov.va.api.health.dataquery.service.controller.medication.MedicationSamples.Dstu2.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medication.MedicationSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.medication.MedicationSamples.Dstu2;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.resources.Medication;
import gov.va.api.health.dstu2.api.resources.Medication.Bundle;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.ids.api.Registration;
import gov.va.api.health.ids.api.ResourceIdentity;
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
public class Dstu2MedicationControllerTest {
  HttpServletResponse response = mock(HttpServletResponse.class);

  private IdentityService ids = mock(IdentityService.class);

  @Autowired private MedicationRepository repository;

  @SneakyThrows
  private MedicationEntity asEntity(DatamartMedication dm) {
    return MedicationEntity.builder()
        .cdwId(dm.cdwId())
        .payload(JacksonConfig.createMapper().writeValueAsString(dm))
        .build();
  }

  Dstu2MedicationController controller() {
    return new Dstu2MedicationController(
        new Dstu2Bundler(
            new ConfigurableBaseUrlPageLinks("http://fonzy.com", "cool", "cool", "cool")),
        repository,
        WitnessProtection.builder().identityService(ids).build());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  public void mockMedicationIdentity(String publicId, String cdwId) {
    ResourceIdentity resourceIdentity =
        ResourceIdentity.builder().system("CDW").resource("MEDICATION").identifier(cdwId).build();
    when(ids.lookup(publicId)).thenReturn(List.of(resourceIdentity));
    when(ids.register(Mockito.any()))
        .thenReturn(
            List.of(
                Registration.builder()
                    .uuid(publicId)
                    .resourceIdentities(List.of(resourceIdentity))
                    .build()));
  }

  @Test
  public void read() {
    DatamartMedication dm = Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity("1000", dm.cdwId());
    Medication actual = controller().read("1000");
    assertThat(json(actual)).isEqualTo(json(Dstu2.create().medication("1000")));
  }

  @Test
  public void readRaw() {
    DatamartMedication dm = Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity("1", dm.cdwId());
    String json = controller().readRaw("1", response);
    assertThat(toObject(json)).isEqualTo(dm);
    verify(response).addHeader("X-VA-INCLUDES-ICN", "NONE");
  }

  @Test
  public void readRawThrowsNotFoundWhenDataIsMissing() {
    mockMedicationIdentity("1", "1");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("1", response));
  }

  @Test
  public void readRawThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().readRaw("1", response));
  }

  @Test
  public void readThrowsNotFoundWhenDataIsMissing() {
    mockMedicationIdentity("1", "1");
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("1"));
  }

  @Test
  public void readThrowsNotFoundWhenIdIsUnknown() {
    assertThrows(ResourceExceptions.NotFound.class, () -> controller().read("1"));
  }

  @Test
  public void searchById() {
    DatamartMedication dm = Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity("1", dm.cdwId());
    Bundle actual = controller().searchById("1", 1, 1);
    Medication medication = Dstu2.create().medication("1");
    assertThat(json(actual))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(medication),
                    1,
                    link(LinkRelation.first, "http://fonzy.com/cool/Medication?identifier=1", 1, 1),
                    link(LinkRelation.self, "http://fonzy.com/cool/Medication?identifier=1", 1, 1),
                    link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Medication?identifier=1",
                        1,
                        1))));
  }

  @Test
  public void searchByIdentifier() {
    DatamartMedication dm = Datamart.create().medication();
    repository.save(asEntity(dm));
    mockMedicationIdentity("1", dm.cdwId());
    Bundle actual = controller().searchByIdentifier("1", 1, 1);
    validationSearchByIdResult(actual);
  }

  @SneakyThrows
  private DatamartMedication toObject(String json) {
    return JacksonConfig.createMapper().readValue(json, DatamartMedication.class);
  }

  private void validationSearchByIdResult(Bundle actual) {
    Medication medication = Dstu2.create().medication("1");
    assertThat(json(actual))
        .isEqualTo(
            json(
                Dstu2.asBundle(
                    "http://fonzy.com/cool",
                    List.of(medication),
                    1,
                    Dstu2.link(
                        LinkRelation.first, "http://fonzy.com/cool/Medication?identifier=1", 1, 1),
                    Dstu2.link(
                        LinkRelation.self, "http://fonzy.com/cool/Medication?identifier=1", 1, 1),
                    Dstu2.link(
                        LinkRelation.last,
                        "http://fonzy.com/cool/Medication?identifier=1",
                        1,
                        1))));
  }
}
