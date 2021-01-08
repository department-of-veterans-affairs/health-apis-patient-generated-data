package gov.va.api.health.patientgenerateddata;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class OpenApiController {
  @ResponseBody
  @GetMapping(
      value = {"/r4", "/r4/openapi.json"},
      produces = "application/json")
  public String openApi() {
    return "";
  }
}
