package gov.va.api.health.dataquery.service.controller.observation;

import gov.va.api.health.dataquery.service.controller.datamart.DatamartEntity;
import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@Entity
@Builder
@Table(name = "Observation", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ObservationEntity implements DatamartEntity {
  @Id
  @Column(name = "CDWId")
  @EqualsAndHashCode.Include
  private String cdwId;

  @Column(name = "PatientFullICN")
  private String icn;

  @Column(name = "Category", nullable = true)
  private String category;

  @Column(name = "Code", nullable = true)
  private String code;

  @Column(name = "DateUTC", nullable = true)
  private Instant dateUtc;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  @Column(name = "Observation")
  private String payload;

  static Sort naturalOrder() {
    return Sort.by("cdwId").ascending();
  }

  DatamartObservation asDatamartObservation() {
    return deserializeDatamart(payload, DatamartObservation.class);
  }
}
