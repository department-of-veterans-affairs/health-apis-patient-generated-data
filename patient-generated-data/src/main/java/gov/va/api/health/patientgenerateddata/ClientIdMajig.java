package gov.va.api.health.patientgenerateddata;

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientIdMajig {
  private final ClientIdMap ids;

  @Builder
  @Autowired
  @SneakyThrows
  ClientIdMajig(@Value("${client-ids}") String clientIds) {
    checkState(!"unset".equals(clientIds), "client-ids is unset");
    ids = JacksonMapperConfig.createMapper().readValue(clientIds, ClientIdMap.class);
    for (var entry : ids.ids().entrySet()) {
      log.info("ID {} maps to name {}", entry.getKey(), entry.getValue());
    }
  }

  @Builder
  @lombok.Value
  public static final class ClientIdMap {
    Map<String, String> ids;
  }
}
