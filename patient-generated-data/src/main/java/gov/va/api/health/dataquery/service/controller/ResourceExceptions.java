package gov.va.api.health.dataquery.service.controller;

import java.net.URLDecoder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.util.MultiValueMap;

@UtilityClass
public final class ResourceExceptions {
  @SneakyThrows
  private static String decode(String value) {
    return URLDecoder.decode(value, "UTF-8");
  }

  private static Stream<String> toKeyValueString(@NonNull Map.Entry<String, List<String>> entry) {
    return entry.getValue().stream().map((value) -> entry.getKey() + '=' + decode(value));
  }

  private static String toParametersString(MultiValueMap<String, String> parameters) {
    if (parameters == null || parameters.isEmpty()) {
      return "";
    }
    StringBuilder msg = new StringBuilder();
    String params =
        parameters.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .flatMap(ResourceExceptions::toKeyValueString)
            .collect(Collectors.joining("&"));
    msg.append('?').append(params);
    return msg.toString();
  }

  public static final class InvalidDatamartPayload extends ResourcesException {
    public InvalidDatamartPayload(String id) {
      super(String.format("Resource %s has invalid datamart payload", id));
    }

    public InvalidDatamartPayload(String id, Throwable cause) {
      super(String.format("Resource %s has invalid datamart payload", id), cause);
    }
  }

  public static final class MissingSearchParameters extends ResourcesException {
    public MissingSearchParameters(MultiValueMap<String, String> parameters) {
      super(toParametersString(parameters));
    }

    public MissingSearchParameters(String message) {
      super(message);
    }
  }

  public static final class NotFound extends ResourcesException {
    public NotFound(MultiValueMap<String, String> parameters) {
      this(toParametersString(parameters));
    }

    public NotFound(String message) {
      super(message);
    }
  }

  public static final class NotImplemented extends ResourcesException {
    public NotImplemented(MultiValueMap<String, String> parameters) {
      this(toParametersString(parameters));
    }

    public NotImplemented(String message) {
      super(message);
    }
  }

  public static final class BadSearchParameter extends ResourcesException {
    public BadSearchParameter(MultiValueMap<String, String> parameters) {
      this(toParametersString(parameters));
    }

    public BadSearchParameter(String message) {
      super(message);
    }
  }

  static class ResourcesException extends RuntimeException {
    ResourcesException(String message, Throwable cause) {
      super(message, cause);
    }

    ResourcesException(String message) {
      super(message);
    }
  }

  static final class SearchFailed extends ResourcesException {
    public SearchFailed(MultiValueMap<String, String> parameters, Exception cause) {
      super(toParametersString(parameters), cause);
    }

    public SearchFailed(MultiValueMap<String, String> parameters, String message) {
      super(toParametersString(parameters) + " Reason: " + message);
    }
  }

  static final class UnknownIdentityInSearchParameter extends ResourcesException {
    public UnknownIdentityInSearchParameter(
        MultiValueMap<String, String> parameters, Exception cause) {
      super(toParametersString(parameters), cause);
    }
  }

  static final class UnknownResource extends ResourcesException {
    public UnknownResource(MultiValueMap<String, String> parameters) {
      super(toParametersString(parameters));
    }
  }
}
