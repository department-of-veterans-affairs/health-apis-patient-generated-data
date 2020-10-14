package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.autoconfig.configuration.JacksonConfig.createMapper;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dstu2.api.datatypes.Address;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint;
import gov.va.api.health.dstu2.api.datatypes.ContactPoint.ContactPointUse;
import gov.va.api.health.dstu2.api.datatypes.HumanName;
import gov.va.api.health.dstu2.api.datatypes.Identifier;
import gov.va.api.health.dstu2.api.elements.Extension;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.api.health.dstu2.api.resources.Patient;
import gov.va.api.health.dstu2.api.resources.Patient.Gender;
import java.util.Optional;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public final class DatamartPatientTest {

  @Test
  public void address() {
    assertThat(Dstu2PatientTransformer.address(DatamartPatient.Address.builder().build())).isNull();
    assertThat(Dstu2PatientTransformer.address(null)).isNull();
  }

  Coding coding(String system, String code, String display) {
    return Coding.builder().system(system).code(code).display(display).build();
  }

  @Test
  public void contact() {
    assertThat(DatamartData.create().patient().contact()).isNotEmpty();
    assertThat(
            Dstu2PatientTransformer.contact(
                DatamartPatient.Contact.builder().relationship("someRelationship").build()))
        .isNull();
    assertThat(Dstu2PatientTransformer.contact(null)).isNull();
  }

  @Test
  public void contactPointUse() {
    assertThat(
            Dstu2PatientTransformer.contactPointUse(
                DatamartPatient.Telecom.builder().type("PATIENT CELL PHONE").build()))
        .isEqualTo(ContactPoint.ContactPointUse.mobile);
    assertThat(
            Dstu2PatientTransformer.contactPointUse(
                DatamartPatient.Telecom.builder().type("PATIENT RESIDENCE").build()))
        .isEqualTo(ContactPoint.ContactPointUse.home);
    assertThat(
            Dstu2PatientTransformer.contactPointUse(
                DatamartPatient.Telecom.builder().type("PATIENT EMAIL").build()))
        .isEqualTo(ContactPoint.ContactPointUse.home);
    assertThat(
            Dstu2PatientTransformer.contactPointUse(
                DatamartPatient.Telecom.builder().type("PATIENT PAGER").build()))
        .isEqualTo(ContactPoint.ContactPointUse.home);
    assertThat(
            Dstu2PatientTransformer.contactPointUse(
                DatamartPatient.Telecom.builder().type("PATIENT EMPLOYER").build()))
        .isEqualTo(ContactPoint.ContactPointUse.work);
    assertThat(
            Dstu2PatientTransformer.contactPointUse(
                DatamartPatient.Telecom.builder().type("SPOUSE EMPLOYER").build()))
        .isEqualTo(ContactPoint.ContactPointUse.work);
    assertThat(
            Dstu2PatientTransformer.contactPointUse(
                DatamartPatient.Telecom.builder().type("TEMPORARY").build()))
        .isEqualTo(ContactPoint.ContactPointUse.temp);
    assertThat(
            Dstu2PatientTransformer.contactPointUse(
                DatamartPatient.Telecom.builder().type("BATPHONE").build()))
        .isNull();
    assertThat(Dstu2PatientTransformer.contactPointUse(null)).isNull();
  }

  @Test
  public void contactTelecoms() {
    DatamartPatient email =
        DatamartPatient.builder()
            .telecom(asList(DatamartPatient.Telecom.builder().email("sample@example.etc").build()))
            .build();
    assertThat(tx(email).telecom()).isNotEmpty();
    assertThat(
            Dstu2PatientTransformer.contactTelecoms(
                DatamartPatient.Contact.Phone.builder().phoneNumber("(555)666-7777").build()))
        .isNotEmpty();
    assertThat(
            Dstu2PatientTransformer.contactTelecoms(
                DatamartPatient.Contact.Phone.builder().workPhoneNumber("(777)666-5555").build()))
        .isNotEmpty();
    assertThat(
            Dstu2PatientTransformer.contactTelecoms(
                DatamartPatient.Contact.Phone.builder().email("sample@example.etc").build()))
        .isNotEmpty();
    assertThat(Dstu2PatientTransformer.contactTelecoms(null)).isNull();
  }

  @Test
  public void deceased() {
    DatamartPatient unparseable = DatamartPatient.builder().deathDateTime("unparseable").build();
    assertThat(tx(unparseable).deceasedDateTime()).isNull();
    DatamartPatient deceasedBool = DatamartPatient.builder().deceased("Y").build();
    assertThat(tx(deceasedBool).deceasedBoolean()).isTrue();
    DatamartPatient deceasedDt =
        DatamartPatient.builder().deathDateTime("2013-11-16T02:33:33").build();
    assertThat(tx(deceasedDt).deceasedDateTime()).isEqualTo("2013-11-16T02:33:33Z");
  }

  @Test
  public void ethnicityDisplay() {
    assertThat(Dstu2PatientTransformer.ethnicityDisplay(null)).isNull();
    assertThat(
            Dstu2PatientTransformer.ethnicityDisplay(
                DatamartPatient.Ethnicity.builder().hl7("2135-2").build()))
        .isEqualTo("Hispanic or Latino");
    assertThat(
            Dstu2PatientTransformer.ethnicityDisplay(
                DatamartPatient.Ethnicity.builder().hl7("2186-5").build()))
        .isEqualTo("Non Hispanic or Latino");
    assertThat(
            Dstu2PatientTransformer.ethnicityDisplay(
                DatamartPatient.Ethnicity.builder().hl7("else").display("other").build()))
        .isEqualTo("other");
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  public void maritalStatus() {
    DatamartPatient ms =
        DatamartPatient.builder()
            .maritalStatus(
                DatamartPatient.MaritalStatus.builder().code("nope").abbrev("nada").build())
            .build();
    assertThat(tx(ms).maritalStatus()).isNull();
    assertThat(Dstu2PatientTransformer.maritalStatusCoding(null)).isNull();
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("A").display()).isEqualTo("Annulled");
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("D").display()).isEqualTo("Divorced");
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("I").display())
        .isEqualTo("Interlocutory");
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("L").display())
        .isEqualTo("Legally Separated");
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("M").display()).isEqualTo("Married");
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("P").display()).isEqualTo("Polygamous");
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("S").display())
        .isEqualTo("Never Married");
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("T").display())
        .isEqualTo("Domestic partner");
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("W").display()).isEqualTo("Widowed");
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("UNK").display()).isEqualTo("unknown");
    assertThat(Dstu2PatientTransformer.maritalStatusCoding("uNk").display()).isEqualTo("unknown");
  }

  @Test
  public void name() {
    assertThat(
            Dstu2PatientTransformer.name(
                DatamartPatient.Contact.builder().name("DRAKE,BOBBY").build()))
        .isEqualTo(HumanName.builder().text("DRAKE,BOBBY").build());
    assertThat(Dstu2PatientTransformer.name(DatamartPatient.Contact.builder().name("").build()))
        .isNull();
    assertThat(Dstu2PatientTransformer.name(null)).isNull();
  }

  private DatamartPatient patientSample() {
    return DatamartPatient.builder()
        .objectType("Patient")
        .objectVersion(1)
        .fullIcn("111222333V000999")
        .ssn("999727566")
        .name("Olson653, Conrad619")
        .lastName("Olson653")
        .firstName("Conrad619")
        .birthDateTime("1948-06-28T02:33:33")
        .deceased("Y")
        .deathDateTime("2013-11-16T02:33:33")
        .gender("M")
        .selfIdentifiedGender(Optional.of("Male"))
        .religion(Optional.of("None"))
        .managingOrganization(Optional.of("17229:I"))
        .maritalStatus(
            DatamartPatient.MaritalStatus.builder()
                .display("SEPARATED")
                .abbrev("NULL")
                .code("S")
                .build())
        .ethnicity(
            DatamartPatient.Ethnicity.builder()
                .display("HISPANIC OR LATINO")
                .abbrev("H")
                .hl7("2135-2")
                .build())
        .race(
            asList(
                DatamartPatient.Race.builder()
                    .display("WHITE, NOT OF HISPANIC ORIGIN")
                    .abbrev("6")
                    .build()))
        .telecom(
            asList(
                DatamartPatient.Telecom.builder()
                    .type("Patient Cell Phone")
                    .phoneNumber("555-294-5041")
                    .build(),
                DatamartPatient.Telecom.builder()
                    .type("Patient Email")
                    .email("Conrad619.Olson653@email.example")
                    .build()))
        .address(
            asList(
                DatamartPatient.Address.builder()
                    .type("Legal Residence")
                    .street1("716 Flatley Heights")
                    .city("Montgomery")
                    .state("Alabama")
                    .postalCode("36043")
                    .country("USA")
                    .build()))
        .contact(
            asList(
                DatamartPatient.Contact.builder()
                    .name("UNK,UNKO")
                    .type("Emergency Contact")
                    .relationship("WIFE")
                    .phone(
                        DatamartPatient.Contact.Phone.builder()
                            .phoneNumber("(0909)000-1234")
                            .workPhoneNumber("(0999)000-1234")
                            .build())
                    .address(
                        DatamartPatient.Address.builder()
                            .street1("1501 ROXAS BOULEVARD")
                            .city("PASAY CITY, METRO MANILA")
                            .state("PHILIPPINES")
                            .build())
                    .build()))
        .build();
  }

  @Test
  public void raceCoding() {
    String codingSystem = "http://hl7.org/fhir/v3/Race";
    assertThat(Dstu2PatientTransformer.raceCoding(null)).isNull();
    assertThat(
            Dstu2PatientTransformer.raceCoding(
                DatamartPatient.Race.builder().display("INDIAN").build()))
        .isEqualTo(coding(codingSystem, "1002-5", "American Indian or Alaska Native"));
    assertThat(
            Dstu2PatientTransformer.raceCoding(
                DatamartPatient.Race.builder().display("ALASKA").build()))
        .isEqualTo(coding(codingSystem, "1002-5", "American Indian or Alaska Native"));
    assertThat(
            Dstu2PatientTransformer.raceCoding(
                DatamartPatient.Race.builder().display("ASIAN").build()))
        .isEqualTo(coding(codingSystem, "2028-9", "Asian"));
    assertThat(
            Dstu2PatientTransformer.raceCoding(
                DatamartPatient.Race.builder().display("BLACK").build()))
        .isEqualTo(coding(codingSystem, "2054-5", "Black or African American"));
    assertThat(
            Dstu2PatientTransformer.raceCoding(
                DatamartPatient.Race.builder().display("AFRICA").build()))
        .isEqualTo(coding(codingSystem, "2054-5", "Black or African American"));
    assertThat(
            Dstu2PatientTransformer.raceCoding(
                DatamartPatient.Race.builder().display("HAWAII").build()))
        .isEqualTo(coding(codingSystem, "2076-8", "Native Hawaiian or Other Pacific Islander"));
    assertThat(
            Dstu2PatientTransformer.raceCoding(
                DatamartPatient.Race.builder().display("PACIFIC").build()))
        .isEqualTo(coding(codingSystem, "2076-8", "Native Hawaiian or Other Pacific Islander"));
    assertThat(
            Dstu2PatientTransformer.raceCoding(
                DatamartPatient.Race.builder().display("WHITE").build()))
        .isEqualTo(coding(codingSystem, "2106-3", "White"));
    assertThat(
            Dstu2PatientTransformer.raceCoding(
                DatamartPatient.Race.builder().display("OTHER").build()))
        .isEqualTo(coding("http://hl7.org/fhir/v3/NullFlavor", "UNK", "Unknown"));
    assertThat(
            Dstu2PatientTransformer.raceCoding(
                DatamartPatient.Race.builder().display("AsIAn").build()))
        .isEqualTo(coding(codingSystem, "2028-9", "Asian"));
  }

  @Test
  public void relationshipCoding() {
    Coding.CodingBuilder cb =
        Coding.builder().system("http://hl7.org/fhir/patient-contact-relationship");
    assertThat(
            Dstu2PatientTransformer.relationshipCoding(
                DatamartPatient.Contact.builder().type("CIVIL GUARDIAN").build()))
        .isEqualTo(cb.code("guardian").display("Guardian").build());
    assertThat(
            Dstu2PatientTransformer.relationshipCoding(
                DatamartPatient.Contact.builder().type("VA GUARDIAN").build()))
        .isEqualTo(cb.code("guardian").display("Guardian").build());
    assertThat(
            Dstu2PatientTransformer.relationshipCoding(
                DatamartPatient.Contact.builder().type("EMERGENCY CONTACT").build()))
        .isEqualTo(cb.code("emergency").display("Emergency").build());
    assertThat(
            Dstu2PatientTransformer.relationshipCoding(
                DatamartPatient.Contact.builder().type("SECONDARY EMERGENCY CONTACT").build()))
        .isEqualTo(cb.code("emergency").display("Emergency").build());
    assertThat(
            Dstu2PatientTransformer.relationshipCoding(
                DatamartPatient.Contact.builder().type("NEXT OF KIN").build()))
        .isEqualTo(cb.code("family").display("Family").build());
    assertThat(
            Dstu2PatientTransformer.relationshipCoding(
                DatamartPatient.Contact.builder().type("SECONDARY NEXT OF KIN").build()))
        .isEqualTo(cb.code("family").display("Family").build());
    assertThat(
            Dstu2PatientTransformer.relationshipCoding(
                DatamartPatient.Contact.builder().type("SPOUSE EMPLOYER").build()))
        .isEqualTo(cb.code("family").display("Family").build());
    assertThat(Dstu2PatientTransformer.relationshipCoding(null)).isNull();
  }

  @Test
  public void sortNum() {
    DatamartPatient dm =
        DatamartPatient.builder()
            .telecom(
                asList(
                    DatamartPatient.Telecom.builder().email("three@temp.com").build(),
                    DatamartPatient.Telecom.builder()
                        .phoneNumber("(222)222-2222")
                        .type("PATIENT RESIDENCE")
                        .build(),
                    DatamartPatient.Telecom.builder().workPhoneNumber("(444)444-4444").build(),
                    DatamartPatient.Telecom.builder()
                        .phoneNumber("(111)111-1111")
                        .type("PATIENT CELL PHONE")
                        .build()))
            .build();
    Patient pat = tx(dm);
    assertThat(pat.telecom().get(0).value()).isEqualTo("1111111111");
    assertThat(pat.telecom().get(1).value()).isEqualTo("2222222222");
    assertThat(pat.telecom().get(2).value()).isEqualTo("three@temp.com");
    assertThat(pat.telecom().get(3).value()).isEqualTo("4444444444");
  }

  public Patient tx(DatamartPatient dmPatient) {
    return Dstu2PatientTransformer.builder().datamart(dmPatient).build().toFhir();
  }

  @Test
  @SneakyThrows
  public void unmarshalSample() {
    DatamartPatient dm =
        createMapper()
            .readValue(
                getClass().getResourceAsStream("datamart-patient.json"), DatamartPatient.class);
    assertThat(dm).isEqualTo(patientSample());
  }

  @Builder
  @Value
  private static class DatamartData {

    @Builder.Default String icn = "1011537977V693883";

    @Builder.Default String ssn = "000001234";

    @Builder.Default String name = "TEST,PATIENT ONE";

    @Builder.Default String firstName = "PATIENT ONE";

    @Builder.Default String lastName = "TEST";

    @Builder.Default String birthDateTime = "1925-01-01T00:00:00";

    static DatamartData create() {
      return DatamartData.builder().build();
    }

    DatamartPatient patient() {
      return DatamartPatient.builder()
          .objectType("Patient")
          .objectVersion(1)
          .fullIcn(icn)
          .ssn(ssn)
          .name(name)
          .firstName(firstName)
          .lastName(lastName)
          .birthDateTime(birthDateTime)
          .deceased("N")
          .gender("M")
          .maritalStatus(DatamartPatient.MaritalStatus.builder().abbrev("UNK").build())
          .ethnicity(DatamartPatient.Ethnicity.builder().hl7("2135-2").build())
          .race(asList(DatamartPatient.Race.builder().display("asian").build()))
          .telecom(
              asList(
                  DatamartPatient.Telecom.builder()
                      .type("confidential")
                      .phoneNumber("021234567")
                      .build(),
                  DatamartPatient.Telecom.builder()
                      .type("patient cell phone")
                      .phoneNumber("011 9991234567")
                      .build(),
                  DatamartPatient.Telecom.builder()
                      .type("patient residence")
                      .phoneNumber("(0900)000-1234")
                      .workPhoneNumber("(0900)000-1234")
                      .build(),
                  DatamartPatient.Telecom.builder()
                      .type("temporary")
                      .phoneNumber("(02)771-9342")
                      .build()))
          .address(
              asList(
                  DatamartPatient.Address.builder()
                      .type("Temporary")
                      .street1("HOTEL PASAY")
                      .street2("232 KAMAGONG ST")
                      .city("PASAY")
                      .state("*Missing*")
                      .postalCode("01300")
                      .country("PHILIPPINES")
                      .build(),
                  DatamartPatient.Address.builder()
                      .type("Confidential")
                      .street1("1501 ROXAS BLVD")
                      .city("PASAY CITY")
                      .state("*Missing*")
                      .postalCode("01302")
                      .country("PHILIPPINES")
                      .build(),
                  DatamartPatient.Address.builder()
                      .type("Patient")
                      .street1("55555 ROXAS BOULEVARD")
                      .city("PASAY CITY")
                      .state("*Missing*")
                      .postalCode("01302")
                      .country("PHILIPPINES")
                      .build()))
          .contact(
              asList(
                  DatamartPatient.Contact.builder()
                      .name("UNK,UNKO")
                      .type("Emergency Contact")
                      .relationship("WIFE")
                      .phone(
                          DatamartPatient.Contact.Phone.builder()
                              .phoneNumber("(0909)000-1234")
                              .workPhoneNumber("(0999)000-1234")
                              .email("sample@example.com")
                              .build())
                      .address(
                          DatamartPatient.Address.builder()
                              .street1("1501 ROXAS BOULEVARD")
                              .street2(null)
                              .street3(null)
                              .city("PASAY CITY, METRO MANILA")
                              .state("PHILIPPINES")
                              .postalCode(null)
                              .county(null)
                              .country(null)
                              .build())
                      .build()))
          .build();
    }

    @Builder
    @Value
    private static class FhirData {

      @Builder.Default String icn = "1011537977V693883";

      @Builder.Default String name = "TEST,PATIENT ONE";

      @Builder.Default String firstName = "PATIENT ONE";

      @Builder.Default String lastName = "TEST";

      static FhirData from(DatamartData dm) {
        return FhirData.builder()
            .icn(dm.icn())
            .name(dm.name())
            .firstName(dm.firstName())
            .lastName(dm.lastName())
            .build();
      }

      public Patient patient() {
        return Patient.builder()
            .id(icn)
            .resourceType("Patient")
            .extension(
                asList(
                    Extension.builder()
                        .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-race")
                        .extension(
                            asList(
                                Extension.builder()
                                    .url("ombCategory")
                                    .valueCoding(
                                        Coding.builder()
                                            .system("http://hl7.org/fhir/v3/Race")
                                            .code("2028-9")
                                            .display("Asian")
                                            .build())
                                    .build(),
                                Extension.builder().url("text").valueString("Asian").build()))
                        .build(),
                    Extension.builder()
                        .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-ethnicity")
                        .extension(
                            asList(
                                Extension.builder()
                                    .url("ombCategory")
                                    .valueCoding(
                                        Coding.builder()
                                            .system("http://hl7.org/fhir/ValueSet/v3-Ethnicity")
                                            .code("2135-2")
                                            .display("Hispanic or Latino")
                                            .build())
                                    .build(),
                                Extension.builder()
                                    .url("text")
                                    .valueString("Hispanic or Latino")
                                    .build()))
                        .build(),
                    Extension.builder()
                        .url("http://fhir.org/guides/argonaut/StructureDefinition/argo-birthsex")
                        .valueCode("M")
                        .build()))
            .identifier(
                asList(
                    Identifier.builder()
                        .use(Identifier.IdentifierUse.usual)
                        .type(
                            CodeableConcept.builder()
                                .coding(
                                    asList(
                                        Coding.builder()
                                            .system("http://hl7.org/fhir/v2/0203")
                                            .code("MR")
                                            .build()))
                                .build())
                        .system("http://va.gov/mvi")
                        .value(icn)
                        .assigner(Reference.builder().display("Master Veteran Index").build())
                        .build(),
                    Identifier.builder()
                        .use(Identifier.IdentifierUse.official)
                        .type(
                            CodeableConcept.builder()
                                .coding(
                                    asList(
                                        Coding.builder()
                                            .system("http://hl7.org/fhir/v2/0203")
                                            .code("SB")
                                            .build()))
                                .build())
                        .system("http://hl7.org/fhir/sid/us-ssn")
                        .value("000001234")
                        .assigner(
                            Reference.builder()
                                .display("United States Social Security Number")
                                .build())
                        .build()))
            .name(
                asList(
                    HumanName.builder()
                        .use(HumanName.NameUse.usual)
                        .text(name)
                        .family(asList(lastName))
                        .given(asList(firstName))
                        .build()))
            .telecom(
                asList(
                    ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.phone)
                        .value("011 9991234567")
                        .use(ContactPoint.ContactPointUse.mobile)
                        .build(),
                    ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.phone)
                        .value("09000001234")
                        .use(ContactPoint.ContactPointUse.home)
                        .build(),
                    ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.phone)
                        .value("027719342")
                        .use(ContactPoint.ContactPointUse.temp)
                        .build(),
                    ContactPoint.builder()
                        .system(ContactPoint.ContactPointSystem.phone)
                        .value("09000001234")
                        .use(ContactPoint.ContactPointUse.work)
                        .build()))
            .gender(Gender.male)
            .birthDate("1925-01-01")
            .deceasedBoolean(false)
            .address(
                asList(
                    Address.builder()
                        .line(asList("HOTEL PASAY", "232 KAMAGONG ST"))
                        .city("PASAY")
                        .state("*Missing*")
                        .postalCode("01300")
                        .country("PHILIPPINES")
                        .build(),
                    Address.builder()
                        .line(asList("1501 ROXAS BLVD"))
                        .city("PASAY CITY")
                        .state("*Missing*")
                        .postalCode("01302")
                        .country("PHILIPPINES")
                        .build(),
                    Address.builder()
                        .line(asList("55555 ROXAS BOULEVARD"))
                        .city("PASAY CITY")
                        .state("*Missing*")
                        .postalCode("01302")
                        .country("PHILIPPINES")
                        .build()))
            .maritalStatus(
                CodeableConcept.builder()
                    .coding(
                        asList(
                            Coding.builder()
                                .system("http://hl7.org/fhir/v3/NullFlavor")
                                .code("UNK")
                                .display("unknown")
                                .build()))
                    .build())
            .contact(
                asList(
                    Patient.Contact.builder()
                        .name(HumanName.builder().text("UNK,UNKO").build())
                        .relationship(
                            asList(
                                CodeableConcept.builder()
                                    .coding(
                                        asList(
                                            Coding.builder()
                                                .system(
                                                    "http://hl7.org/fhir/patient-contact-relationship")
                                                .code("emergency")
                                                .display("Emergency")
                                                .build()))
                                    .text("Emergency Contact")
                                    .build()))
                        .telecom(
                            asList(
                                ContactPoint.builder()
                                    .system(ContactPoint.ContactPointSystem.phone)
                                    .use(ContactPointUse.home)
                                    .value("09090001234")
                                    .build(),
                                ContactPoint.builder()
                                    .system(ContactPoint.ContactPointSystem.phone)
                                    .use(ContactPointUse.work)
                                    .value("09990001234")
                                    .build(),
                                ContactPoint.builder()
                                    .system(ContactPoint.ContactPointSystem.email)
                                    .value("sample@example.com")
                                    .build()))
                        .address(
                            Address.builder()
                                .line(asList("1501 ROXAS BOULEVARD"))
                                .city("PASAY CITY, METRO MANILA")
                                .state("PHILIPPINES")
                                .build())
                        .build()))
            .build();
      }
    }
  }
}
