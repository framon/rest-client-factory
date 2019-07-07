package br.eti.monzeu.restclientfactory;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.web.client.RestTemplate;

public interface CredentialsConfigurator {

  void configureHttpClient(HttpClientBuilder builder, CredentialsProvider credentialsProvider);

  void configureRestTemplate(RestTemplate builder);
}
