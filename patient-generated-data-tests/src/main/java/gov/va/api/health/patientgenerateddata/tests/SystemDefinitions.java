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
                .systemAndCode(
                    "venue$https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier|534/12975")
                .systemWithAnyCode(
                    "venue$https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier|")
                .build())
        .questionnaireList(
            "37953b72-961b-41ee-bd05-86c62bacc46b," + "842479ed-9c5b-474b-bf97-fc295617900c")
        .questionnaireUpdates("00000000-0000-0000-0000-000000000000")
        .questionnaireResponse("f003043a-9047-4c3a-b15b-a26c67f4e723")
        .questionnaireResponseAuthor("1011537977V693883")
        .questionnaireResponseMetas(
            Ids.Metas.builder()
                .applicationTag(
                    Ids.MetaTag.builder()
                        .system("https://api.va.gov/services/pgd")
                        .code("66a5960c-68ee-4689-88ae-4c7cccf7ca79")
                        .build())
                .appointmentTag(
                    Ids.MetaTag.builder()
                        .system(
                            "https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-appointment-identifier")
                        .code("202008211400983000082100000000000000")
                        .build())
                .build())
        .questionnaireResponseSource("1011537977V693883")
        .questionnaireResponseSubject("1011537977V693883")
        .questionnaireResponseUpdates("00000000-0000-0000-0000-000000000000")
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

    @NonNull String questionnaireList;

    @NonNull String questionnaireUpdates;

    @NonNull String questionnaireResponse;

    @NonNull String questionnaireResponseAuthor;

    @NonNull Metas questionnaireResponseMetas;

    @NonNull String questionnaireResponseSource;

    @NonNull String questionnaireResponseSubject;

    @NonNull String questionnaireResponseUpdates;

    @Value
    @Builder
    static final class Metas {
      @NonNull MetaTag applicationTag;

      @NonNull MetaTag appointmentTag;
    }

    @Value
    @Builder
    static final class MetaTag {
      @NonNull String system;

      @NonNull String code;
    }

    @Value
    @Builder
    static final class UsageContextTypeValue {
      @NonNull String codeWithAnySystem;

      @NonNull String codeWithNoSystem;

      @NonNull String systemAndCode;

      @NonNull String systemWithAnyCode;
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
