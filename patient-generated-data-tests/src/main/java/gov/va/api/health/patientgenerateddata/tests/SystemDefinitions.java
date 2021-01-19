package gov.va.api.health.patientgenerateddata.tests;

import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.SentinelProperties;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
class SystemDefinitions {
  static final String CLIENT_KEY_DEFAULT = "pteracuda";

  private static Ids ids() {
    return Ids.builder()
        .observation("fc691a7f-a0f3-47b4-9d00-2786d055e8ba")
        .patient("1011537977V693883")
        .patientGenerated("9999999999V999999")
        .patientNotMe("1017283180V801730")
        .questionnaire("37953b72-961b-41ee-bd05-86c62bacc46b")
        .questionnaireContextTypeValue(
            Ids.UsageContextTypeValue.builder()
                .codeWithAnySystem("venue$534/12975")
                .codeWithNoSystem("venue$|534/12975")
                .systemAndCode("venue$https://staff.apps.va.gov/VistaEmrService/clinics|534/12975")
                .systemWithAnyCode("venue$https://staff.apps.va.gov/VistaEmrService/clinics|")
                .build())
        .questionnaireResponse("f003043a-9047-4c3a-b15b-a26c67f4e723")
        .questionnaireResponseAuthor("1011537977V693883")
        .questionnaireResponseSubject("1011537977V693883")
        .questionnaireResponseUpdates("4400ade6-4162-44e2-b470-c6b38c717f88")
        .questionnaireResponseMetaTag(
            Ids.MetaTag.builder()
                .metaTagSystem("https://veteran.apps.va.gov/appointments/v1")
                .metaTagCode("202008211400983000082100000000000000")
                .build())
        .build();
  }

  private static SystemDefinition lab() {
    return SystemDefinition.builder()
        .internal(
            serviceDefinition(
                "internal", "https://blue.lab.lighthouse.va.gov", 443, "/patient-generated-data/"))
        .r4(serviceDefinition("r4", "https://blue.lab.lighthouse.va.gov", 443, "/pgd/v0/r4/"))
        .ids(ids())
        .build();
  }

  private static SystemDefinition local() {
    return SystemDefinition.builder()
        .internal(serviceDefinition("internal", "http://localhost", 8095, "/"))
        .r4(serviceDefinition("r4", "http://localhost", 8095, "/r4/"))
        .ids(ids())
        .build();
  }

  private static SystemDefinition production() {
    return SystemDefinition.builder()
        .internal(
            serviceDefinition(
                "internal",
                "https://blue.production.lighthouse.va.gov",
                443,
                "/patient-generated-data/"))
        .r4(
            serviceDefinition(
                "r4", "https://blue.production.lighthouse.va.gov", 443, "/pgd/v0/r4/"))
        .ids(ids())
        .build();
  }

  private static SystemDefinition qa() {
    return SystemDefinition.builder()
        .internal(
            serviceDefinition(
                "internal", "https://blue.qa.lighthouse.va.gov", 443, "/patient-generated-data/"))
        .r4(serviceDefinition("r4", "https://blue.qa.lighthouse.va.gov", 443, "/pgd/v0/r4/"))
        .ids(ids())
        .build();
  }

  private static Service serviceDefinition(String name, String url, int port, String apiPath) {
    return Service.builder()
        .url(SentinelProperties.optionUrl(name, url))
        .port(port)
        .apiPath(SentinelProperties.optionApiPath(name, apiPath))
        .build();
  }

  private static SystemDefinition staging() {
    return SystemDefinition.builder()
        .internal(
            serviceDefinition(
                "internal",
                "https://blue.staging.lighthouse.va.gov",
                443,
                "/patient-generated-data/"))
        .r4(serviceDefinition("r4", "https://blue.staging.lighthouse.va.gov", 443, "/pgd/v0/r4/"))
        .ids(ids())
        .build();
  }

  private static SystemDefinition stagingLab() {
    return SystemDefinition.builder()
        .internal(
            serviceDefinition(
                "internal",
                "https://blue.staging-lab.lighthouse.va.gov",
                443,
                "/patient-generated-data/"))
        .r4(
            serviceDefinition(
                "r4", "https://blue.staging-lab.lighthouse.va.gov", 443, "/pgd/v0/r4/"))
        .ids(ids())
        .build();
  }

  static SystemDefinition systemDefinition() {
    switch (Environment.get()) {
      case LOCAL:
        return local();
      case QA:
        return qa();
      case STAGING:
        return staging();
      case PROD:
        return production();
      case STAGING_LAB:
        return stagingLab();
      case LAB:
        return lab();
      default:
        throw new IllegalArgumentException(
            "Unsupported sentinel environment: " + Environment.get());
    }
  }

  @Value
  @Builder
  static final class Ids {
    @NonNull String observation;

    @NonNull String patient;

    @NonNull String patientGenerated;

    @NonNull String patientNotMe;

    @NonNull String questionnaire;

    @NonNull UsageContextTypeValue questionnaireContextTypeValue;

    @NonNull MetaTag questionnaireResponseMetaTag;

    @NonNull String questionnaireResponse;

    @NonNull String questionnaireResponseAuthor;

    @NonNull String questionnaireResponseSubject;

    @NonNull String questionnaireResponseUpdates;

    @Value
    @Builder
    static final class UsageContextTypeValue {
      @NonNull String codeWithAnySystem;

      @NonNull String codeWithNoSystem;

      @NonNull String systemAndCode;

      @NonNull String systemWithAnyCode;
    }

    @Value
    @Builder
    static final class MetaTag {
      @NonNull String metaTagSystem;

      @NonNull String metaTagCode;
    }
  }

  @Value
  @Builder
  static final class Service {
    @NonNull String url;

    @NonNull Integer port;

    @NonNull String apiPath;

    String urlWithApiPath() {
      StringBuilder builder = new StringBuilder(url());
      if (!apiPath().startsWith("/")) {
        builder.append('/');
      }
      builder.append(apiPath());
      if (!apiPath.endsWith("/")) {
        builder.append('/');
      }
      return builder.toString();
    }
  }

  @Value
  @Builder
  static final class SystemDefinition {
    @NonNull Service internal;

    @NonNull Service r4;

    @NonNull Ids ids;
  }
}
