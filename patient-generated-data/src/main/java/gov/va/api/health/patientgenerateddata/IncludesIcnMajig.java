package gov.va.api.health.patientgenerateddata;

import static java.util.stream.Collectors.joining;

import gov.va.api.health.r4.api.elements.Reference;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Builder
public final class IncludesIcnMajig<T, B> implements ResponseBodyAdvice<Object> {
  public static final String INCLUDES_ICN_HEADER = "X-VA-INCLUDES-ICN";

  @NonNull private final Class<T> type;

  @NonNull private final Class<B> bundleType;

  @NonNull private final Function<B, Stream<T>> extractResources;

  @NonNull private final Function<T, Stream<String>> extractIcns;

  /** Add X-VA-INCLUDES-ICN header if it does not already exist. */
  public static void addHeader(ServerHttpResponse serverHttpResponse, String usersCsv) {
    HttpHeaders headers = serverHttpResponse != null ? serverHttpResponse.getHeaders() : null;
    if (headers == null
        || headers.get(INCLUDES_ICN_HEADER) == null
        || headers.get(INCLUDES_ICN_HEADER).isEmpty()) {
      serverHttpResponse.getHeaders().add(INCLUDES_ICN_HEADER, usersCsv);
    }
  }

  /** Extract patient ICN from the reference. */
  public static String icn(Reference reference) {
    if ("patient".equalsIgnoreCase(ReferenceUtils.resourceType(reference))) {
      return ReferenceUtils.resourceId(reference);
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object beforeBodyWrite(
      Object payload,
      MethodParameter unused1,
      MediaType unused2,
      Class<? extends HttpMessageConverter<?>> unused3,
      ServerHttpRequest unused4,
      ServerHttpResponse serverHttpResponse) {
    if (type.isInstance(payload)) {
      String users = extractIcns.apply((T) payload).collect(joining(","));
      users = users.isBlank() ? "NONE" : users;
      addHeader(serverHttpResponse, users);
      return payload;
    }

    if (bundleType.isInstance(payload)) {
      String users =
          extractResources
              .apply((B) payload)
              .flatMap(resource -> extractIcns.apply(resource))
              .distinct()
              .collect(joining(","));
      users = users.isBlank() ? "NONE" : users;
      addHeader(serverHttpResponse, users);
      return payload;
    }

    return payload;
  }

  @Override
  public boolean supports(
      MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> unused) {
    return type.equals(methodParameter.getParameterType())
        || bundleType.equals(methodParameter.getParameterType())
        || ResponseEntity.class.equals(methodParameter.getParameterType());
  }
}
