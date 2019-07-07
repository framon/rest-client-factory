package br.eti.monzeu.restclientfactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Fábrica de RestTemplate pré-configurado.
 *
 * @author Fábio Ramon
 */
public class RestTemplateFactory implements FactoryBean<RestTemplate> {

  private static final Logger log = LoggerFactory.getLogger(RestTemplateFactory.class);

  private List<CredentialsConfigurator> credentialsConfigurators;
  private LoggingRequestInterceptor loggingRequestInterceptor;
  private ObjectMapper objectMapper;
  private Integer timeout;
  private boolean traceEnabled;

  public RestTemplateFactory() {
    credentialsConfigurators = List.of();
    loggingRequestInterceptor = new LoggingRequestInterceptor();
    traceEnabled = LoggerFactory.getLogger(getClass()).isTraceEnabled();
  }

  protected HttpClient newHttpClient() {
    HttpClientBuilder builder = HttpClients.custom();
    setupRequestTimeout(builder);

    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

    log.trace("Configuring HttpClient credentials");
    for (CredentialsConfigurator credentialsConfigurator : credentialsConfigurators) {
      credentialsConfigurator.configureHttpClient(builder, credentialsProvider);
    }
    builder.setDefaultCredentialsProvider(credentialsProvider);

    log.trace("Building HttpClient");
    return builder.build();
  }

  protected void setupRequestTimeout(HttpClientBuilder builder) {
    log.trace("Configuring HttpClient timeouts");
    RequestConfig.Builder requestBuilder = RequestConfig.custom();
    requestBuilder = requestBuilder.setSocketTimeout(timeout);
    requestBuilder = requestBuilder.setConnectTimeout(timeout);
    requestBuilder = requestBuilder.setConnectionRequestTimeout(timeout);

    log.debug("Setting connection timeout to '{}'", timeout);
    builder.setDefaultRequestConfig(requestBuilder.build());
  }

  protected ClientHttpRequestFactory newClientHttpRequestFactory() {
    return new HttpComponentsClientHttpRequestFactory(newHttpClient());
  }

  protected RestTemplate newRestTemplate() {
    log.trace("Constructing RestTemplate");
    boolean isTraceRequest = traceEnabled && loggingRequestInterceptor != null;
    ClientHttpRequestFactory clientHttpRequestFactory = newClientHttpRequestFactory();

    log.debug("isTrace enable? {}", isTraceRequest);

    if (isTraceRequest) {
      clientHttpRequestFactory = new BufferingClientHttpRequestFactory(clientHttpRequestFactory);
    }

    RestTemplate rt = new RestTemplate(clientHttpRequestFactory);

    log.debug("Setting ObjectMapper {}", objectMapper);
    if (objectMapper != null) {
      MappingJackson2HttpMessageConverter jacksonMessageConverter = new MappingJackson2HttpMessageConverter();
      jacksonMessageConverter.setObjectMapper(objectMapper);
      HttpMessageConverter<?> messageConverter = jacksonMessageConverter;
      rt.getMessageConverters().add(0, messageConverter);
    }

    log.trace("Configuring RestTemplate credentials");
    for (CredentialsConfigurator credentialsConfigurator : credentialsConfigurators) {
      credentialsConfigurator.configureRestTemplate(rt);
    }

    if (isTraceRequest) {
      rt.getInterceptors().add(loggingRequestInterceptor);
    }

    return rt;
  }

  public void setCredentialsConfigurators(List<CredentialsConfigurator> credentialsConfigurators) {
    this.credentialsConfigurators = credentialsConfigurators;
  }

  public void setLoggingRequestInterceptor(LoggingRequestInterceptor loggingRequestInterceptor) {
    this.loggingRequestInterceptor = loggingRequestInterceptor;
  }

  public void setTraceEnabled(boolean traceEnabled) {
    this.traceEnabled = traceEnabled;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public RestTemplate getObject() {
    return newRestTemplate();
  }

  @Override
  public Class<?> getObjectType() {
    return RestTemplate.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
