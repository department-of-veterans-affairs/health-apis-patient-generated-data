package gov.va.api.health.patientgenerateddata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.HandlerExceptionResolver;

public class InternalClientKeyFilterConfigurationTest {
  @Test
  @SneakyThrows
  void match() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("client-key")).thenReturn("topsecret");
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);

    InternalClientKeyFilterConfiguration.ClientKeyFilter.builder()
        .clientKey("topsecret")
        .resolver(resolver)
        .build()
        .doFilterInternal(request, response, chain);

    verify(chain, times(1)).doFilter(request, response);
  }

  @Test
  @SneakyThrows
  void mismatch() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("client-key")).thenReturn("wrong");
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);

    InternalClientKeyFilterConfiguration.ClientKeyFilter.builder()
        .clientKey("topsecret")
        .resolver(resolver)
        .build()
        .doFilterInternal(request, response, chain);

    verify(chain, times(0)).doFilter(request, response);
    verify(resolver, times(1))
        .resolveException(eq(request), eq(response), eq(null), any(Exceptions.Unauthorized.class));
  }

  @Test
  @SneakyThrows
  void noHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);

    InternalClientKeyFilterConfiguration.ClientKeyFilter.builder()
        .clientKey("topsecret")
        .resolver(resolver)
        .build()
        .doFilterInternal(request, response, chain);

    verify(chain, times(0)).doFilter(request, response);
    verify(resolver, times(1))
        .resolveException(eq(request), eq(response), eq(null), any(Exceptions.Unauthorized.class));
  }
}
