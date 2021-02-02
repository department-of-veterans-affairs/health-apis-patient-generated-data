package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TokenListMappingJpaTest {
  @Autowired JdbcTemplate jdbc;

  @Autowired FooRepository repository;

  private static QuestionnaireResponse _questionnaireResponse(
      String valueSystem, String valueCode) {
    return QuestionnaireResponse.builder()
        .meta(
            Meta.builder()
                .tag(List.of(Coding.builder().code(valueCode).system(valueSystem).build()))
                .build())
        .build();
  }

  private static MockHttpServletRequest _requestFromUri(String uri) {
    var u = UriComponentsBuilder.fromUriString(uri).build();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(u.getPath());
    request.setRemoteHost(u.getHost());
    request.setProtocol(u.getScheme());
    request.setServerPort(u.getPort());
    u.getQueryParams()
        .entrySet()
        .forEach(e -> request.addParameter(e.getKey(), e.getValue().toArray(new String[0])));
    return request;
  }

  @Test
  void search_tag_edgeCases() {
    jdbc.execute("create table app.foo (id varchar, value varchar)");
    String tagJoin = TokenListMapping.metadataTagJoin(_questionnaireResponse("clinics", "123"));
    FooEntity entity = FooEntity.builder().id("x").value(tagJoin).build();
    repository.save(entity);
    TokenListMapping<FooEntity> mapping =
        TokenListMapping.<FooEntity>builder().parameterName("param").fieldName("value").build();

    Specification<FooEntity> spec =
        mapping.specificationFor(_requestFromUri("http://fizz.com?param=,"));
    assertThat(spec).isNull();

    spec = mapping.specificationFor(_requestFromUri("http://fizz.com?param=|,|"));
    assertThat(spec).isNull();

    spec = mapping.specificationFor(_requestFromUri("http://fizz.com?param=,|"));
    assertThat(spec).isNull();

    spec = mapping.specificationFor(_requestFromUri("http://fizz.com?param=|,"));
    assertThat(spec).isNull();
  }

  @Test
  void specificationFor_csv() {
    jdbc.execute("create table app.foo (id varchar, value varchar)");
    String tagJoin = TokenListMapping.metadataTagJoin(_questionnaireResponse("clinics", "123"));
    String tagJoinSecondary =
        TokenListMapping.metadataTagJoin(_questionnaireResponse("something", "456"));
    FooEntity entity = FooEntity.builder().id("x").value(tagJoin).build();
    FooEntity entitySecondary = FooEntity.builder().id("y").value(tagJoinSecondary).build();
    repository.save(entity);
    repository.save(entitySecondary);
    TokenListMapping<FooEntity> mapping =
        TokenListMapping.<FooEntity>builder().parameterName("param").fieldName("value").build();
    Specification<FooEntity> spec =
        mapping.specificationFor(
            _requestFromUri("http://fizz.com?param=clinics|123,something|456"));
    assertThat(repository.findAll(spec)).isEqualTo(List.of(entity, entitySecondary));
  }

  @Test
  void specificationFor_systemAndCode() {
    jdbc.execute("create table app.foo (id varchar, value varchar)");
    String tagJoin = TokenListMapping.metadataTagJoin(_questionnaireResponse("clinics", "123"));
    FooEntity entity = FooEntity.builder().id("x").value(tagJoin).build();
    repository.save(entity);
    TokenListMapping<FooEntity> mapping =
        TokenListMapping.<FooEntity>builder().parameterName("param").fieldName("value").build();
    Specification<FooEntity> spec =
        mapping.specificationFor(_requestFromUri("http://fizz.com?param=clinics|123"));
    assertThat(repository.findAll(spec)).isEqualTo(List.of(entity));
  }
}
