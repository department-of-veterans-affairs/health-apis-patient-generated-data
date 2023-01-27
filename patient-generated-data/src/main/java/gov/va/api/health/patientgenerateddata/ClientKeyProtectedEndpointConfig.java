package gov.va.api.health.patientgenerateddata;

import static gov.va.api.lighthouse.talos.Responses.unauthorizedAsJson;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.lighthouse.talos.ClientKeyProtectedEndpointFilter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ClientKeyProtectedEndpointConfig {
  private static final String CLIENT_KEY_HEADER = "patient-generated-data.client-keys";

  @Bean
  FilterRegistrationBean<ClientKeyProtectedEndpointFilter> clientKeyProtectedEndpointFilter(
      @Value("${" + CLIENT_KEY_HEADER + ":unset}") String clientKeysCsv) {
    var registration = new FilterRegistrationBean<ClientKeyProtectedEndpointFilter>();

    registration.setOrder(1);
    List<String> clientKeys;

    if (isBlank(clientKeysCsv) || "unset".equals(clientKeysCsv)) {
      throw new IllegalStateException(
          CLIENT_KEY_HEADER
              + " must be populated and not 'unset'. "
              + "Provide a client-key, or disable with 'disabled'.");
    }

    if ("disabled".equals(clientKeysCsv)) {
      log.warn(
          "Client-key protection is disabled. To enable, "
              + "set "
              + CLIENT_KEY_HEADER
              + " to a value other than 'disabled'.");

      registration.setEnabled(false);
      clientKeys = List.of();
    } else {
      log.info(
          "ClientKeyProtectedEndpointFilter enabled with priority {}", registration.getOrder());
      clientKeys = Arrays.asList(clientKeysCsv.split(",", -1));
    }

    registration.setFilter(
        ClientKeyProtectedEndpointFilter.builder()
            .clientKeys(clientKeys)
            .name("Patient-Generated-Data Request")
            .unauthorizedResponse(unauthorizedResponse())
            .build());

    registration.setUrlPatterns(
        Stream.concat(
                Stream.of("/r4/*", "/management/*"),
                PathRewriteConfig.leadingPaths().stream().map(s -> s + "r4/*"))
            .toList());

    return registration;
  }

  @SneakyThrows
  private Consumer<HttpServletResponse> unauthorizedResponse() {
    return unauthorizedAsJson(
        """
{
   "id": "patient-generated-data",
   "status": "Unauthorized",
   "message": "Check the client-key header."
}
""");
  }
}
