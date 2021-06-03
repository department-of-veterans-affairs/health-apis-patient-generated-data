package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.MockRequests.requestFromUri;
import static gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseSamples.questionnaireResponse;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TokenListMappingJpaTest {
  @Autowired JdbcTemplate jdbc;

  @Autowired FooRepository repository;

  @Test
  void specificationFor_csv() {
    jdbc.execute("create table app.foo (id varchar, value varchar)");
    String tagJoin = TokenListMapping.metadataTagJoin(questionnaireResponse("clinics", "123"));
    String tagJoinSecondary =
        TokenListMapping.metadataTagJoin(questionnaireResponse("something", "456"));
    FooEntity entity = FooEntity.builder().id("x").value(tagJoin).build();
    FooEntity entitySecondary = FooEntity.builder().id("y").value(tagJoinSecondary).build();
    repository.save(entity);
    repository.save(entitySecondary);
    TokenListMapping<FooEntity> mapping =
        TokenListMapping.<FooEntity>builder().parameterName("param").fieldName("value").build();
    Specification<FooEntity> spec =
        mapping.specificationFor(requestFromUri("http://fizz.com?param=clinics|123,something|456"));
    assertThat(repository.findAll(spec)).isEqualTo(List.of(entity, entitySecondary));
  }

  @Test
  void specificationFor_edgeCases() {
    jdbc.execute("create table app.foo (id varchar, value varchar)");
    String tagJoin = TokenListMapping.metadataTagJoin(questionnaireResponse("clinics", "123"));
    FooEntity entity = FooEntity.builder().id("x").value(tagJoin).build();
    repository.save(entity);
    TokenListMapping<FooEntity> mapping =
        TokenListMapping.<FooEntity>builder().parameterName("param").fieldName("value").build();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=,"))).isNull();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=|,|"))).isNull();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=,|"))).isNull();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=|,"))).isNull();
  }

  @Test
  void specificationFor_systemAndCode() {
    jdbc.execute("create table app.foo (id varchar, value varchar)");
    String tagJoin = TokenListMapping.metadataTagJoin(questionnaireResponse("clinics", "123"));
    FooEntity entity = FooEntity.builder().id("x").value(tagJoin).build();
    repository.save(entity);
    TokenListMapping<FooEntity> mapping =
        TokenListMapping.<FooEntity>builder().parameterName("param").fieldName("value").build();
    Specification<FooEntity> spec =
        mapping.specificationFor(requestFromUri("http://fizz.com?param=clinics|123"));
    assertThat(repository.findAll(spec)).isEqualTo(List.of(entity));
  }
}
