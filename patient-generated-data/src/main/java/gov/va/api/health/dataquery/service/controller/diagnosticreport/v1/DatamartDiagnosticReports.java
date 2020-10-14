package gov.va.api.health.dataquery.service.controller.diagnosticreport.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatamartDiagnosticReports {
  @Builder.Default private String objectType = "DiagnosticReport";

  @Builder.Default private int objectVersion = 1;

  private String fullIcn;

  private String patientName;

  @JsonProperty("DiagnosticReports")
  private List<DiagnosticReport> reports;

  /** Lazy getter. */
  public List<DiagnosticReport> reports() {
    if (reports == null) {
      reports = new ArrayList<>();
    }
    return reports;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class DiagnosticReport {
    private String identifier;

    private String sta3n;

    private String effectiveDateTime;

    private String issuedDateTime;

    private String accessionInstitutionSid;

    private String accessionInstitutionName;

    private String institutionSid;

    private String institutionName;

    private String verifyingStaffSid;

    private String verifyingStaffName;

    private String topographySid;

    private String topographyName;

    private String visitSid;

    private String visitCategory;

    private List<Order> orders;

    private List<Result> results;

    private String reportStatus;

    /** Lazy Getter. */
    public List<Order> orders() {
      if (orders == null) {
        orders = new ArrayList<>();
      }
      return orders;
    }

    /** Lazy Getter. */
    public List<Result> results() {
      if (results == null) {
        results = new ArrayList<>();
      }
      return results;
    }

    /** Backwards compatibility for etlDate. */
    @SuppressWarnings("unused")
    @JsonProperty("ETLEditDateTime")
    private void setEtlDate(String unused) {
      /* no op */
    }
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Order {
    private String sid;

    private String display;
  }

  @Data
  @Builder
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Result {
    private String result;

    private String display;
  }
}
