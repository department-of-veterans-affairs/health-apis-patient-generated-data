package gov.va.api.health.patientgenerateddata;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
  private static final Object OPEN_API = initOpenApi();

  @SneakyThrows
  private static String initOpenApi() {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(mapper.readTree(readOpenApi()));
  }

  @SneakyThrows
  private static String readOpenApi() {
    try (InputStream is = new ClassPathResource("openapi.json").getInputStream()) {
      return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
    }
  }

  @ResponseBody
  @GetMapping(
      value = {"r4/", "r4/openapi.json"},
      produces = "application/json")
  public Object openApi() {
    return OPEN_API;
  }
}
