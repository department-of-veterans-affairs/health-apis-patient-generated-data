package gov.va.api.health.patientgenerateddata.observation;

import gov.va.api.health.patientgenerateddata.PayloadEntity;
import gov.va.api.health.r4.api.resources.Observation;
import java.time.Instant;
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
import org.springframework.data.domain.Sort;

/** Database entity representing an Observation. */
@Data
@Entity
@Builder
@Table(name = "Observation", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ObservationEntity implements PayloadEntity<Observation> {
  @Id @EqualsAndHashCode.Include private String id;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  private String payload;

  @Version private Integer version;

  private Instant lastUpdated;

  public static Sort naturalOrder() {
    return Sort.by("id").ascending();
  }

  @Override
  public Class<Observation> resourceType() {
    return Observation.class;
  }
}
