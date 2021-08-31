package gov.va.api.health.patientgenerateddata;

import static com.google.common.base.Preconditions.checkState;
import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nimbusds.jose.JWSObject;
import java.text.ParseException;
import java.util.Map;
import lombok.Builder;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Sourcerer {
  private final Map<String, String> clientIds;

  private final String staticAccessToken;

  /** Autowired constructor. */
  @Builder
  @Autowired
  @SneakyThrows
  public Sourcerer(
      @Value("${authorization.client-ids}") String clientIds,
      @Value("${authorization.static-access-token}") String staticAccessToken) {
    checkState(!"unset".equals(clientIds), "authorization.client-ids is unset");
    checkState(!"unset".equals(staticAccessToken), "authorization.static-access-token is unset");
    this.clientIds =
        JacksonMapperConfig.createMapper()
            .readValue(clientIds, new TypeReference<Map<String, String>>() {});
    this.staticAccessToken = staticAccessToken;
  }

  /** placeholder lol. */
  @SneakyThrows
  public String source(String authorization) {
    checkRequestState(
        authorization.startsWith("Bearer "), "Invalid authorization: %s", authorization);
    String token = authorization.substring(7);
    if (token.equals(staticAccessToken)) {
      return "https://api.va.gov/services/pgd/static-access";
    }
    try {
      JWSObject jwsObject = JWSObject.parse(token);
      Object clientId = jwsObject.getPayload().toJSONObject().get("cid");
      checkRequestState(clientId != null, "Invalid authorization token: %s", token);
      String name = clientIds.get(clientId);
      checkRequestState(name != null, "Invalid authorization client ID: %s", clientId);
      return "https://api.va.gov/services/pgd/" + name;
    } catch (ParseException ex) {
      throw new Exceptions.BadRequest("Invalid authorization token: " + token, ex);
    }
  }
}
