package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import gov.va.api.health.autoconfig.logging.Loggable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** QuestionnaireResponse Spring Repository. */
@Loggable
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface QuestionnaireResponseRepository
    extends CrudRepository<QuestionnaireResponseEntity, String>,
        JpaSpecificationExecutor<QuestionnaireResponseEntity> {}
