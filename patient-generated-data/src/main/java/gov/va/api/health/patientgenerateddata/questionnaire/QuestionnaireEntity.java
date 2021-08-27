package gov.va.api.health.patientgenerateddata.questionnaire;

import gov.va.api.health.patientgenerateddata.PayloadEntity;
import gov.va.api.health.r4.api.resources.Questionnaire;
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

@Data
@Entity
@Builder
@Table(name = "Questionnaire", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuestionnaireEntity implements PayloadEntity<Questionnaire> {
  @Id @EqualsAndHashCode.Include private String id;

  @Version private Integer version;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  private String payload;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  private String contextTypeValue;

  private Instant lastUpdated;

  public static Sort naturalOrder() {
    return Sort.by("id").ascending();
  }

  @Override
  public Class<Questionnaire> resourceType() {
    return Questionnaire.class;
  }
}
