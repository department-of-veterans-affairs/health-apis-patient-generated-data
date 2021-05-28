package gov.va.api.health.patientgenerateddata.questionnaire;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Questionnaire Spring Repository. */
@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface QuestionnaireRepository
    extends CrudRepository<QuestionnaireEntity, String>,
        JpaSpecificationExecutor<QuestionnaireEntity> {}
