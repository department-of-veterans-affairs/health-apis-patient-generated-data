package gov.va.api.health.patientgenerateddata;

import static com.google.common.base.Preconditions.checkState;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class InternalClientKeyFilterConfiguration {

  @Autowired
  @Qualifier("handlerExceptionResolver")
  private HandlerExceptionResolver resolver;

  /** Registers filter bean to be used by Spring. */
  @Bean
  public FilterRegistrationBean<ClientKeyFilter> filterRegistration(
      @Value("${internal.client-key}") String clientKey) {
    checkState(!"unset".equals(clientKey), "internal.client-key is unset");
    FilterRegistrationBean<ClientKeyFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(
        ClientKeyFilter.builder().clientKey(clientKey).resolver(resolver).build());
    registrationBean.addUrlPatterns("/management/*");
    return registrationBean;
  }

  @Builder
  static final class ClientKeyFilter extends OncePerRequestFilter {
    @NonNull private final String clientKey;

    @NonNull private HandlerExceptionResolver resolver;

    @Override
    @SneakyThrows
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
      String requestKey = request.getHeader("client-key");
      try {
        verify(requestKey);
        filterChain.doFilter(request, response);
      } catch (Exception e) {
        resolver.resolveException(request, response, null, e);
      }
    }

    private void verify(String requestKey) {
      if (!clientKey.equals(requestKey)) {
        throw new Exceptions.Unauthorized("Invalid token for request header: client-key");
      }
    }
  }
}
