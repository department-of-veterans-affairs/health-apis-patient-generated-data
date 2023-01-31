package gov.va.api.health.patientgenerateddata;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"patient-generated-data.client-keys=disabled"})
public class ApplicationTest {
  @Test
  void contextLoads() {}
}
