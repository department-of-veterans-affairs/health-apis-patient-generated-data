package gov.va.api.health.patientgenerateddata;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

// @Loggable
// @Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface FooRepository
    extends CrudRepository<FooEntity, String>, JpaSpecificationExecutor<FooEntity> {}
