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
@Table(name = "QuestionnaireResponse", schema = "app")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuestionnaireResponseEntity implements PayloadEntity {
  @Id @EqualsAndHashCode.Include private String id;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  private String payload;

  @Version private Integer version;

  private Instant authored;

  private String author;

  public static Sort naturalOrder() {
    return Sort.by("id").ascending();
  }

  @Override
  public QuestionnaireResponse deserializePayload() {
    return deserializePayload(QuestionnaireResponse.class);
  }
}
