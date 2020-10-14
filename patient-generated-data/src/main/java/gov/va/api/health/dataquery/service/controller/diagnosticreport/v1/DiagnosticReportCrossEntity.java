package gov.va.api.health.dataquery.service.controller.diagnosticreport.v1;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@Table(name = "DiagnosticReport_CrossWalk", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiagnosticReportCrossEntity implements DatamartEntity {
  @Id
  @EqualsAndHashCode.Include
  @Column(name = "Identifier")
  private String reportId;

  @Column(name = "PatientFullICN")
  private String icn;

  @Override
  public String cdwId() {
    return reportId();
  }
}
