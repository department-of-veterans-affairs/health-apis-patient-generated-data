package gov.va.api.health.patientgenerateddata.tests;

import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.SentinelProperties;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
class SystemDefinitions {
  private static Ids ids() {
    return Ids.builder()
        .lastUpdated("gt2021-01-01T00:00:00Z")
        .observation("fc691a7f-a0f3-47b4-9d00-2786d055e8ba")
        .observationList(
            List.of(
                "fc691a7f-a0f3-47b4-9d00-2786d055e8ba",
                "8cdc73e7-56e1-4823-b1ad-9b107c33d9a2",
                "0b9d2e37-f84d-4f9e-9ba3-995772f368d3"))
        .observationSubject("1011537977V693883")
        .questionnaire("37953b72-961b-41ee-bd05-86c62bacc46b")
        .questionnaireContextTypeValue(
            Ids.UsageContextTypeValue.builder()
                .codeWithAnySystem("venue$vha_688_3485")
                .codeWithNoSystem("venue$|vha_688_3485")
                .systemAndCode(
                    "venue$https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier|vha_688_3485")
                .systemWithAnyCode(
                    "venue$https://api.va.gov/services/fhir/v0/r4/NamingSystem/va-clinic-identifier|")
                .build())
        .questionnaireList(
            List.of("37953b72-961b-41ee-bd05-86c62bacc46b", "842479ed-9c5b-474b-bf97-fc295617900c"))
        .questionnaireUpdates("00000000-0000-0000-0000-000000000000")
        .questionnaireResponse("f003043a-9047-4c3a-b15b-a26c67f4e723")
        .questionnaireResponseAuthor("1011537977V693883")
        .questionnaireResponseList(
            List.of("f003043a-9047-4c3a-b15b-a26c67f4e723", "e7c5799f-14fd-420d-8671-e24386773e7e"))
        .questionnaireResponseMetas(
            Ids.Metas.builder()
                .commonTag(
                    Ids.MetaTag.builder()
                        .system("http://terminology.hl7.org/CodeSystem/common-tags")
                        .code("actionable")
                        .build())
                .repoTag(
                    Ids.MetaTag.builder()
                        .system("https://github.com/department-of-veterans-affairs")
                        .code("health-apis-patient-generated-data")
                        .build())
                .build())
        .questionnaireResponseSource("1011537977V693883")
        .questionnaireResponseSubject("1011537977V693883")
        .questionnaireResponseUpdates("00000000-0000-0000-0000-000000000000")
        .build();
  }

  private static SystemDefinition lab() {
    return SystemDefinition.builder()
        .management(
            serviceDefinition(
                "management",
                "https://blue.lab.lighthouse.va.gov",
                443,
                "/patient-generated-data/management/"))
        .r4(
            serviceDefinition(
                "r4", "https://blue.lab.lighthouse.va.gov", 443, "/patient-generated-data/r4/"))
        .sandboxDataR4(
            serviceDefinition(
                "sandbox-data-r4",
                "https://blue.lab.lighthouse.va.gov",
                443,
                "/patient-generated-data/sandbox-data/r4/"))
        .ids(ids())
        .build();
  }

  private static SystemDefinition local() {
    return SystemDefinition.builder()
        .management(serviceDefinition("management", "http://localhost", 8095, "/management/"))
        .r4(serviceDefinition("r4", "http://localhost", 8095, "/r4/"))
        .sandboxDataR4(
            serviceDefinition("sandbox-data-r4", "http://localhost", 8095, "/sandbox-data/r4/"))
        .ids(ids())
        .build();
  }

  private static SystemDefinition production() {
    return SystemDefinition.builder()
        .management(
            serviceDefinition(
                "management",
                "https://blue.production.lighthouse.va.gov",
                443,
                "/patient-generated-data/management/"))
        .r4(
            serviceDefinition(
                "r4",
                "https://blue.production.lighthouse.va.gov",
                443,
                "/patient-generated-data/r4/"))
        .sandboxDataR4(
            serviceDefinition(
                "sandbox-data-r4",
                "https://blue.production.lighthouse.va.gov",
                443,
                "/patient-generated-data/sandbox-data/r4/"))
        .ids(ids())
        .build();
  }

  private static SystemDefinition qa() {
    return SystemDefinition.builder()
        .management(
            serviceDefinition(
                "management",
                "https://blue.qa.lighthouse.va.gov",
                443,
                "/patient-generated-data/management/"))
        .r4(
            serviceDefinition(
                "r4", "https://blue.qa.lighthouse.va.gov", 443, "/patient-generated-data/r4/"))
        .sandboxDataR4(
            serviceDefinition(
                "sandbox-data-r4",
                "https://blue.qa.lighthouse.va.gov",
                443,
                "/patient-generated-data/sandbox-data/r4/"))
        .ids(ids())
        .build();
  }

  private static Service serviceDefinition(String name, String url, int port, String apiPath) {
    return Service.builder()
        .url(SentinelProperties.optionUrl(name, url))
        .port(SentinelProperties.optionPort(name, port))
        .apiPath(SentinelProperties.optionApiPath(name, apiPath))
        .build();
  }

  private static SystemDefinition staging() {
    return SystemDefinition.builder()
        .management(
            serviceDefinition(
                "management",
                "https://blue.staging.lighthouse.va.gov",
                443,
                "/patient-generated-data/management/"))
        .r4(
            serviceDefinition(
                "r4", "https://blue.staging.lighthouse.va.gov", 443, "/patient-generated-data/r4/"))
        .sandboxDataR4(
            serviceDefinition(
                "sandbox-data-r4",
                "https://blue.staging.lighthouse.va.gov",
                443,
                "/patient-generated-data/sandbox-data/r4/"))
        .ids(ids())
        .build();
  }

  private static SystemDefinition stagingLab() {
    return SystemDefinition.builder()
        .management(
            serviceDefinition(
                "management",
                "https://blue.staging-lab.lighthouse.va.gov",
                443,
                "/patient-generated-data/management/"))
        .r4(
            serviceDefinition(
                "r4",
                "https://blue.staging-lab.lighthouse.va.gov",
                443,
                "/patient-generated-data/r4/"))
        .sandboxDataR4(
            serviceDefinition(
                "sandbox-data-r4",
                "https://blue.staging-lab.lighthouse.va.gov",
                443,
                "/patient-generated-data/sandbox-data/r4/"))
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
    @NonNull String lastUpdated;

    @NonNull String observation;

    @NonNull List<String> observationList;

    @NonNull String observationSubject;

    @NonNull String questionnaire;

    @NonNull UsageContextTypeValue questionnaireContextTypeValue;

    @NonNull List<String> questionnaireList;

    @NonNull String questionnaireUpdates;

    @NonNull String questionnaireResponse;

    @NonNull String questionnaireResponseAuthor;

    @NonNull List<String> questionnaireResponseList;

    @NonNull Metas questionnaireResponseMetas;

    @NonNull String questionnaireResponseSource;

    @NonNull String questionnaireResponseSubject;

    @NonNull String questionnaireResponseUpdates;

    @Value
    @Builder
    static final class Metas {
      @NonNull MetaTag commonTag;

      @NonNull MetaTag repoTag;
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
      builder.append(":");
      builder.append(port());
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
    @NonNull Service management;

    @NonNull Service r4;

    @NonNull Service sandboxDataR4;

    @NonNull Ids ids;
  }
}
