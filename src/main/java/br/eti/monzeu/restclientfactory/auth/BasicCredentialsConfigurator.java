package br.eti.monzeu.restclientfactory.auth;

import br.eti.monzeu.restclientfactory.CredentialsConfigurator;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

public class BasicCredentialsConfigurator implements CredentialsConfigurator {

  private static final Logger log = LoggerFactory.getLogger(BasicCredentialsConfigurator.class);

  private String username;
  private String password;

  public BasicCredentialsConfigurator(String username, String password) {
    this.username = username;
    this.password = password;
  }

  @Override
  public void configureHttpClient(HttpClientBuilder builder,
      CredentialsProvider credentialsProvider) {
    log.debug("Configuring HttpClient with any scope and username {}", username);

    final AuthScope authscope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
        AuthScope.ANY_REALM);

    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
    credentialsProvider.setCredentials(authscope, credentials);
    log.info("CredentialsProvider {} configured", credentials.getClass());
  }

  @Override
  public void configureRestTemplate(RestTemplate builder) {
  }

}
