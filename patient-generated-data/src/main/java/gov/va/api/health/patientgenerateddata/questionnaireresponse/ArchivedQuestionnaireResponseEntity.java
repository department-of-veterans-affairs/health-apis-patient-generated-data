package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import gov.va.api.health.patientgenerateddata.PayloadEntity;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.time.Instant;
import javax.persistence.Basic;
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

@Data
@Entity
@Builder
@Table(name = "ArchivedQuestionnaireResponse", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ArchivedQuestionnaireResponseEntity implements PayloadEntity<QuestionnaireResponse> {
  @Id @EqualsAndHashCode.Include private String id;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  private String payload;

  private Instant deletionTimestamp;

  @Override
  public Class<QuestionnaireResponse> resourceType() {
    return QuestionnaireResponse.class;
  }
}
