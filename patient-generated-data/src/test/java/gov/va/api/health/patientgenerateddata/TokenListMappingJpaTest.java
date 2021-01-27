package gov.va.api.health.patientgenerateddata;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireEntity;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseEntity;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TokenListMappingJpaTest {
  @Autowired JdbcTemplate template;

  @Autowired private FooRepository repository;

  @Test
  void doIt() {
    String join = TokenListMapping.metadataTagJoin(_questionnaireResponse("clinics", "123"));
    template.execute("create table app.foo (id varchar, tag varchar)");
    FooEntity entity = FooEntity.builder().id("x").tag(join).build();
    repository.save(entity);

    TokenListMapping<FooEntity> mapping =
        TokenListMapping.<FooEntity>builder().parameterName("tag").fieldName("tag").build();
    Specification<FooEntity> spec =
        mapping.specificationFor(_requestFromUri("http://fizz.com?tag=clinics|123"));
    List<FooEntity> results = repository.findAll(spec);
    assertThat(results).isEqualTo(List.of(entity));
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

  private static QuestionnaireResponse _questionnaireResponse(
      String valueSystem, String valueCode) {
    return QuestionnaireResponse.builder()
        .meta(
            Meta.builder()
                .tag(List.of(Coding.builder().code(valueCode).system(valueSystem).build()))
                .build())
        .build();
  }

  //  private FacilitiesController controller() {
  //    return FacilitiesController.builder()
  //        .facilityRepository(repo)
  //        .baseUrl("http://foo/")
  //        .basePath("bp")
  //        .build();
  //  }

  //  @Test
  //  void geoFacilitiesByZip() {
  //    repo.save(FacilitySamples.defaultSamples().facilityEntity("vha_757"));
  //    assertThat(controller().geoFacilitiesByZip("43219", "HEALTH", List.of("urology"), false, 1,
  // 1))
  //        .isEqualTo(
  //            GeoFacilitiesResponse.builder()
  //                .type(GeoFacilitiesResponse.Type.FeatureCollection)
  //                .features(List.of(FacilitySamples.defaultSamples().geoFacility("vha_757")))
  //                .build());
  //  }
  //  @Test
  //  void jsonFacilitiesByZip_invalidService() {
  //    assertThrows(
  //        ExceptionsV0.InvalidParameter.class,
  //        () -> controller().jsonFacilitiesByZip("33333", null, List.of("unknown"), null, 1, 1));
  //  }
  //
  //  @Test
  //  void jsonFacilitiesByZip_invalidType() {
  //    assertThrows(
  //        ExceptionsV0.InvalidParameter.class,
  //        () -> controller().jsonFacilitiesByZip("33333", "xxx", null, null, 1, 1));
  //  }
  //
  //  @Test
  //  void jsonFacilitiesByZip_noFilter() {
  //    repo.save(FacilitySamples.defaultSamples().facilityEntity("vha_757"));
  //    assertThat(controller().jsonFacilitiesByZip("43219", null, null, null, 1, 1).data())
  //        .isEqualTo(List.of(FacilitySamples.defaultSamples().facility("vha_757")));
  //  }
  //
  //  @Test
  //  void jsonFacilitiesByZip_perPageZero() {
  //    repo.save(FacilitySamples.defaultSamples().facilityEntity("vha_757"));
  //    assertThat(controller().jsonFacilitiesByZip("43219", null, null, null, 100, 0))
  //        .isEqualTo(
  //            FacilitiesResponse.builder()
  //                .data(emptyList())
  //                .links(
  //                    PageLinks.builder()
  //                        .self("http://foo/bp/v0/facilities?zip=43219&page=100&per_page=0")
  //                        .build())
  //                .meta(
  //                    FacilitiesResponse.FacilitiesMetadata.builder()
  //                        .pagination(
  //                            Pagination.builder()
  //                                .currentPage(100)
  //                                .entriesPerPage(0)
  //                                .totalPages(0)
  //                                .totalEntries(1)
  //                                .build())
  //                        .build())
  //                .build());
  //  }
  //
  //  @Test
  //  void jsonFacilitiesByZip_serviceOnly() {
  //    repo.save(FacilitySamples.defaultSamples().facilityEntity("vha_757"));
  //    assertThat(
  //            controller().jsonFacilitiesByZip("43219", null, List.of("urology"), null, 1,
  // 1).data())
  //        .isEqualTo(List.of(FacilitySamples.defaultSamples().facility("vha_757")));
  //  }
  //
  //  @Test
  //  void jsonFacilitiesByZip_typeAndService() {
  //    repo.save(FacilitySamples.defaultSamples().facilityEntity("vha_757"));
  //    assertThat(
  //            controller().jsonFacilitiesByZip("43219", "HEALTH", List.of("primarycare"), null, 1,
  // 1))
  //        .isEqualTo(
  //            FacilitiesResponse.builder()
  //                .data(List.of(FacilitySamples.defaultSamples().facility("vha_757")))
  //                .links(
  //                    PageLinks.builder()
  //                        .self(
  //
  // "http://foo/bp/v0/facilities?services%5B%5D=primarycare&type=HEALTH&zip=43219&page=1&per_page=1")
  //                        .first(
  //
  // "http://foo/bp/v0/facilities?services%5B%5D=primarycare&type=HEALTH&zip=43219&page=1&per_page=1")
  //                        .last(
  //
  // "http://foo/bp/v0/facilities?services%5B%5D=primarycare&type=HEALTH&zip=43219&page=1&per_page=1")
  //                        .build())
  //                .meta(
  //                    FacilitiesResponse.FacilitiesMetadata.builder()
  //                        .pagination(
  //                            Pagination.builder()
  //                                .currentPage(1)
  //                                .entriesPerPage(1)
  //                                .totalPages(1)
  //                                .totalEntries(1)
  //                                .build())
  //                        .build())
  //                .build());
  //  }
  //
  //  @Test
  //  void jsonFacilitiesByZip_typeOnly() {
  //    repo.save(FacilitySamples.defaultSamples().facilityEntity("vha_757"));
  //    assertThat(controller().jsonFacilitiesByZip("43219", "HEALTH", emptyList(), null, 1,
  // 1).data())
  //        .isEqualTo(List.of(FacilitySamples.defaultSamples().facility("vha_757")));
  //  }
}
