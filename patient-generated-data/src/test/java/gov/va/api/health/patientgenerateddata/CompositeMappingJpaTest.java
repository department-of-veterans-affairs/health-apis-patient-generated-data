package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.MockRequests.requestFromUri;
import static gov.va.api.health.patientgenerateddata.questionnaire.Samples.questionnaireWithUseContext;
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
public class CompositeMappingJpaTest {
  @Autowired JdbcTemplate jdbc;

  @Autowired FooRepository repository;

  @Test
  void specificationFor_edgeCases() {
    jdbc.execute("create table app.foo (id varchar, value varchar)");
    String join =
        CompositeMapping.useContextValueJoin(
            questionnaireWithUseContext("fizz", "buzz", "something", "else"));
    FooEntity entity = FooEntity.builder().id("x").value(join).build();
    repository.save(entity);
    CompositeMapping<FooEntity> mapping =
        CompositeMapping.<FooEntity>builder().parameterName("param").fieldName("value").build();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=,"))).isNull();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=|,|"))).isNull();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=,|"))).isNull();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=|,"))).isNull();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=$,$"))).isNull();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=,$"))).isNull();
    assertThat(mapping.specificationFor(requestFromUri("http://fizz.com?param=$,"))).isNull();
  }

  @Test
  void specificationFor_systemAndCode() {
    jdbc.execute("create table app.foo (id varchar, value varchar)");
    String join =
        CompositeMapping.useContextValueJoin(
            questionnaireWithUseContext("fizz", "buzz", "something", "else"));
    FooEntity entity = FooEntity.builder().id("x").value(join).build();
    repository.save(entity);
    CompositeMapping<FooEntity> mapping =
        CompositeMapping.<FooEntity>builder().parameterName("param").fieldName("value").build();
    Specification<FooEntity> spec =
        mapping.specificationFor(requestFromUri("http://fizz.com?param=buzz$something|else"));
    assertThat(repository.findAll(spec)).isEqualTo(List.of(entity));
  }
}
