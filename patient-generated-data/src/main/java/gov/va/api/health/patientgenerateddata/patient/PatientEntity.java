package gov.va.api.health.patientgenerateddata.patient;

import gov.va.api.health.patientgenerateddata.PayloadEntity;
import gov.va.api.health.r4.api.resources.Patient;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@Table(name = "Patient", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PatientEntity implements PayloadEntity<Patient> {
  @Id @EqualsAndHashCode.Include private String id;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  private String payload;

  @Version private Integer version;

  @Override
  public Class<Patient> resourceType() {
    return Patient.class;
  }
}
