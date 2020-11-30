package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class UrlPageLinksTest {

  @Test
  void urlBuiltCorrectlyWithTrailingSlash() {
    UrlPageLinks pageLinks =
        UrlPageLinks.builder().baseUrl("http://va.gov/").r4BasePath("api/gov").build();
    assertThat(pageLinks.r4Url()).isEqualTo("http://va.gov/api/gov");
  }

  @Test
  void urlBuiltCorrectlyWithoutTrailingSlash() {
    UrlPageLinks pageLinks =
        UrlPageLinks.builder().baseUrl("http://va.gov").r4BasePath("api/gov").build();
    assertThat(pageLinks.r4Url()).isEqualTo("http://va.gov/api/gov");
  }
}
