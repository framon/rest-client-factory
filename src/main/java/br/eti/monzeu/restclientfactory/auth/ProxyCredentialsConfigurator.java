package br.eti.monzeu.restclientfactory.auth;

import br.eti.monzeu.restclientfactory.CredentialsConfigurator;
import java.net.URI;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class ProxyCredentialsConfigurator implements CredentialsConfigurator {

  private static final Logger log = LoggerFactory.getLogger(ProxyCredentialsConfigurator.class);

  private String username;
  private String password;
  private String proxyUrl;

  public ProxyCredentialsConfigurator(String username, String password, String proxyUrl) {
    this.username = username;
    this.password = password;
    this.proxyUrl = proxyUrl;
  }

  @Override
  public void configureHttpClient(HttpClientBuilder builder,
      CredentialsProvider credentialsProvider) {
    log.debug("Configuring HttpClient Proxy {} with any scope and username {}", proxyUrl, username);

    HttpHost proxy = URIUtils.extractHost(URI.create(proxyUrl));
    builder.setProxy(proxy);

    final AuthScope authscope = new AuthScope(proxy.getHostName(), proxy.getPort(),
        AuthScope.ANY_REALM);

    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
    credentialsProvider.setCredentials(authscope, credentials);
    log.info("CredentialsProvider {} configured", credentials.getClass());
  }

  @Override
  public void configureRestTemplate(RestTemplate builder) {
  }

}
