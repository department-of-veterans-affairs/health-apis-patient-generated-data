package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.UsageContext;
import gov.va.api.health.r4.api.resources.Questionnaire;
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
public class CompositeMappingJpaTest {
  @Autowired JdbcTemplate jdbc;

  @Autowired FooRepository repository;

  private static Questionnaire _questionnaire(
      String ucSystem, String ucCode, String valueSystem, String valueCode) {
    return Questionnaire.builder()
        .useContext(
            List.of(
                UsageContext.builder()
                    .code(Coding.builder().system(ucSystem).code(ucCode).build())
                    .valueCodeableConcept(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder().system(valueSystem).code(valueCode).build()))
                            .build())
                    .build()))
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
  void specificationFor_systemAndCode() {
    jdbc.execute("create table app.foo (id varchar, value varchar)");
    String tagJoin =
        CompositeMapping.useContextValueJoin(_questionnaire("fizz", "buzz", "something", "else"));
    FooEntity entity = FooEntity.builder().id("x").value(tagJoin).build();
    repository.save(entity);
    CompositeMapping<FooEntity> mapping =
        CompositeMapping.<FooEntity>builder().parameterName("param").fieldName("value").build();
    Specification<FooEntity> spec =
        mapping.specificationFor(_requestFromUri("http://fizz.com?param=buzz$something|else"));
    assertThat(repository.findAll(spec)).isEqualTo(List.of(entity));
  }
}
