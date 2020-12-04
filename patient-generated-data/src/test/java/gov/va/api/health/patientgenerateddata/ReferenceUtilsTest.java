package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.ReferenceUtils.resourceId;
import static gov.va.api.health.patientgenerateddata.ReferenceUtils.resourceType;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.elements.Reference;
import org.junit.jupiter.api.Test;

public class ReferenceUtilsTest {
  @Test
  void resourceId_null() {
    assertThat(resourceId(Reference.builder().build())).isNull();
    assertThat(resourceId(Reference.builder().reference("no").build())).isNull();
    assertThat(resourceId(Reference.builder().reference("/no").build())).isNull();
    assertThat(resourceId(Reference.builder().reference("no/").build())).isNull();
    assertThat(resourceId(Reference.builder().reference("no/ ").build())).isNull();
    assertThat(resourceId(Reference.builder().reference("whatever/no/").build())).isNull();
    assertThat(resourceId(Reference.builder().reference("/whatever/no/").build())).isNull();
  }

  @Test
  void resourceId_valid() {
    assertThat(resourceId(Reference.builder().reference("/Patient/123V456").build()))
        .isEqualTo("123V456");
    assertThat(resourceId(Reference.builder().reference("Patient/123V456").build()))
        .isEqualTo("123V456");
  }

  @Test
  void resourceType_null() {
    assertThat(resourceType(Reference.builder().build())).isNull();
    assertThat(resourceType(Reference.builder().reference("no").build())).isNull();
    assertThat(resourceType(Reference.builder().reference("/no").build())).isNull();
    assertThat(resourceType(Reference.builder().reference("no/").build())).isNull();
    assertThat(resourceType(Reference.builder().reference("no/ ").build())).isNull();
    assertThat(resourceType(Reference.builder().reference("whatever/no/").build())).isNull();
    assertThat(resourceType(Reference.builder().reference("/whatever/no/").build())).isNull();
  }

  @Test
  void resourceType_valid() {
    assertThat(resourceType(Reference.builder().reference("/patient/123V456").build()))
        .isEqualTo("patient");
    assertThat(resourceType(Reference.builder().reference("patient/123V456").build()))
        .isEqualTo("patient");
  }
}
