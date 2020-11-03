package gov.va.api.health.patientgenerateddata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import gov.va.api.health.r4.api.elements.Narrative;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class WebExceptionHandlerTest {
  private static HttpHeaders jsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  @Test
  void sanitizedMessage_Exception() {
    assertThat(WebExceptionHandler.sanitizedMessage(new RuntimeException("oh noez")))
        .isEqualTo("oh noez");
  }

  @Test
  void sanitizedMessage_JsonEOFException() {
    JsonEOFException ex = mock(JsonEOFException.class);
    when(ex.getLocation()).thenReturn(new JsonLocation(null, 0, 0, 0));
    assertThat(WebExceptionHandler.sanitizedMessage(ex)).isEqualTo("line: 0, column: 0");
  }

  @Test
  void sanitizedMessage_JsonMappingException() {
    JsonMappingException ex = mock(JsonMappingException.class);
    when(ex.getPathReference()).thenReturn("x");
    assertThat(WebExceptionHandler.sanitizedMessage(ex)).isEqualTo("path: x");
  }

  @Test
  void sanitizedMessage_JsonParseException() {
    JsonParseException ex = mock(JsonParseException.class);
    when(ex.getLocation()).thenReturn(new JsonLocation(null, 0, 0, 0));
    assertThat(WebExceptionHandler.sanitizedMessage(ex)).isEqualTo("line: 0, column: 0");
  }

  @Test
  void sanitizedMessage_MismatchedInputException() {
    MismatchedInputException ex = mock(MismatchedInputException.class);
    when(ex.getPathReference()).thenReturn("path");
    assertThat(WebExceptionHandler.sanitizedMessage(ex)).isEqualTo("path: path");
  }

  @Test
  void snafu() {
    OperationOutcome outcome =
        new WebExceptionHandler("")
            .handleSnafu(new IllegalStateException("oh noez"), mock(HttpServletRequest.class));
    assertThat(outcome.id(null).extension(null))
        .isEqualTo(
            OperationOutcome.builder()
                .resourceType("OperationOutcome")
                .text(
                    Narrative.builder()
                        .status(Narrative.NarrativeStatus.additional)
                        .div("<div>Failure: null</div>")
                        .build())
                .issue(
                    List.of(
                        OperationOutcome.Issue.builder()
                            .severity(OperationOutcome.Issue.IssueSeverity.fatal)
                            .code("exception")
                            .build()))
                .build());
  }

  @Test
  void validationException() {
    Set<ConstraintViolation<Foo>> violations =
        Validation.buildDefaultValidatorFactory().getValidator().validate(Foo.builder().build());
    OperationOutcome outcome =
        new WebExceptionHandler("")
            .handleValidationException(
                new ConstraintViolationException(violations), mock(HttpServletRequest.class));
    assertThat(outcome.id(null).extension(null))
        .isEqualTo(
            OperationOutcome.builder()
                .resourceType("OperationOutcome")
                .text(
                    Narrative.builder()
                        .status(Narrative.NarrativeStatus.additional)
                        .div("<div>Failure: null</div>")
                        .build())
                .issue(
                    List.of(
                        OperationOutcome.Issue.builder()
                            .severity(OperationOutcome.Issue.IssueSeverity.fatal)
                            .code("structure")
                            .diagnostics("bar must not be null")
                            .build()))
                .build());
  }

  @Value
  @Builder
  private static final class Foo {
    @NotNull String bar;
  }
}
// @Test
// void invalidParameter() {
// assertThat(
// new WebExceptionHandler("")
// .handleInvalidParameter(new Exceptions.InvalidParameter("services", "x")))
// .isEqualTo(
// ResponseEntity.status(HttpStatus.BAD_REQUEST)
// .headers(jsonHeaders())
// .body(
// ApiError.builder()
// .errors(
// List.of(
// ApiError.ErrorMessage.builder()
// .title("Invalid field value")
// .detail("'x' is not a valid value for 'services'")
// .code("103")
// .status("400")
// .build()))
// .build()));
// }
//
// @Test
// @SneakyThrows
// void methodArgumentNotValid() {
// assertThat(
// new WebExceptionHandler("")
// .handleMethodArgumentNotValidException(
// new MethodArgumentNotValidException(
// new MethodParameter(Foo.class.getDeclaredMethod("equals", Object.class),
// 0),
// mock(BindingResult.class))))
// .isEqualTo(
// ResponseEntity.status(HttpStatus.BAD_REQUEST)
// .headers(jsonHeaders())
// .body(
// ApiError.builder()
// .errors(
// List.of(
// ApiError.ErrorMessage.builder()
// .title("Method argument not valid")
// .detail(
// "Validation failed for argument [0] in public boolean
// gov.va.api.lighthouse.facilities.WebExceptionHandlerTest$Foo.equals(java.lang.Object): ")
// .code("400")
// .status("400")
// .build()))
// .build()));
// }
//
// @Test
// void methodArgumentTypeMismatch() {
// assertThat(
// new WebExceptionHandler("")
// .handleMethodArgumentTypeMismatch(
// new MethodArgumentTypeMismatchException(
// "hello", Integer.class, "foo", null, null)))
// .isEqualTo(
// ResponseEntity.status(HttpStatus.BAD_REQUEST)
// .headers(jsonHeaders())
// .body(
// ApiError.builder()
// .errors(
// List.of(
// ApiError.ErrorMessage.builder()
// .title("Invalid field value")
// .detail("'hello' is not a valid value for 'foo'")
// .code("103")
// .status("400")
// .build()))
// .build()));
// }
//
// @Test
// void notAcceptable() {
// assertThat(
// new WebExceptionHandler("")
// .handleNotAcceptable(
// new HttpMediaTypeNotAcceptableException(List.of(MediaType.ALL))))
// .isEqualTo(
// ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
// .headers(jsonHeaders())
// .body(
// ApiError.builder()
// .errors(
// List.of(
// ApiError.ErrorMessage.builder()
// .title("Not acceptable")
// .detail(
// "The resource could not be returned in the requested
// format")
// .code("406")
// .status("406")
// .build()))
// .build()));
// }
//
// @Test
// void notFound() {
// assertThat(new WebExceptionHandler("").handleNotFound(new Exceptions.NotFound("vha_555")))
// .isEqualTo(
// ResponseEntity.status(HttpStatus.NOT_FOUND)
// .headers(jsonHeaders())
// .body(
// ApiError.builder()
// .errors(
// List.of(
// ApiError.ErrorMessage.builder()
// .title("Record not found")
// .detail("The record identified by vha_555 could not be
// found")
// .code("404")
// .status("404")
// .build()))
// .build()));
// }
//
// @Test
// void unsatisfiedServletRequestParameter() {
// assertThat(
// new WebExceptionHandler("")
// .handleUnsatisfiedServletRequestParameter(
// new UnsatisfiedServletRequestParameterException(
// new String[] {"hello"}, ImmutableMap.of("foo", new String[] {"bar"}))))
// .isEqualTo(
// ResponseEntity.status(HttpStatus.BAD_REQUEST)
// .headers(jsonHeaders())
// .body(
// ApiError.builder()
// .errors(
// List.of(
// ApiError.ErrorMessage.builder()
// .title("Missing parameter")
// .detail(
// "Parameter conditions \"hello\" not met for actual
// request parameters: foo={bar}")
// .code("108")
// .status("400")
// .build()))
// .build()));
// }
