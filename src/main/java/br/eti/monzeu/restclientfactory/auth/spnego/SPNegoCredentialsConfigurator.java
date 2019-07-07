package br.eti.monzeu.restclientfactory.auth.spnego;

import br.eti.monzeu.restclientfactory.CredentialsConfigurator;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

public class SPNegoCredentialsConfigurator implements CredentialsConfigurator,
    ClientHttpRequestInterceptor {

  private static final Logger log = LoggerFactory.getLogger(SPNegoCredentialsConfigurator.class);

  private String username;
  private String password;

  private volatile Subject subject;
  private final ReentrantLock lock = new ReentrantLock();

  public SPNegoCredentialsConfigurator(String username, String password) {
    this.username = username;
    this.password = password;
  }

  @Override
  public void configureHttpClient(HttpClientBuilder builder,
      CredentialsProvider credentialsProvider) {
    log.debug("Configuring HttpClient SPNego with any scope");

    final AuthScope authscope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT,
        AuthScope.ANY_REALM);

    NoOpCredentials credentials = new NoOpCredentials();
    credentialsProvider.setCredentials(authscope, credentials);
    log.info("CredentialsProvider {} configured", credentials.getClass());

    Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
        .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build();
    builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
    log.info("AuthScheme {} configured", AuthSchemes.SPNEGO);
  }

  @Override
  public void configureRestTemplate(RestTemplate builder) {
    log.debug("Configuring RestTemplate SPNego");

//        System.setProperty("sun.security.krb5.debug", "true");
//        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
//        System.setProperty("java.security.krb5.conf", "/apache-tomcat-7.0.39/conf/krb5.ini");

    builder.getInterceptors().add(this);
    log.info("Inteceptador de request {} definido", AuthSchemes.SPNEGO);

    login();
  }

  protected void login() {
    try {
      final String module = "com.sun.security.jgss.login";
      final Configuration config = new SPNegoCredentialsModuleConfig();
      final UsernamePasswordCallBackHandler callbackHandler = new UsernamePasswordCallBackHandler(
          username, password);

      log.debug("Logging on module {} with username {}", module, username);
      LoginContext loginContext = new LoginContext(module, null, callbackHandler, config);
      loginContext.login();

      subject = loginContext.getSubject();
      log.info("Logging done. Subject: {}", subject);
    } catch (LoginException e) {
      throw new RuntimeException(e);
    }
  }

  protected void checkTicketExpiration() {
    log.trace("Verifying Kerberos ticket expiration");
    Set<KerberosTicket> credentials = subject.getPrivateCredentials(KerberosTicket.class);
    KerberosTicket kerberosTicket = credentials.iterator().next();

    if (!kerberosTicket.isCurrent()) {
      log.trace("Kerberos Ticket has been expired. Getting lock");
      lock.lock();
      try {
        credentials = subject.getPrivateCredentials(KerberosTicket.class);
        kerberosTicket = credentials.iterator().next();
        if (!kerberosTicket.isCurrent()) {
          log.info("Kerberos Ticket has been expired. Reissuing");
          /* Não adianta fazer revalidação pois o ticket já está expirado */
          login();
        } else {
          log.trace("Kerberos ticket is valid");
        }
      } finally {
        lock.unlock();
      }
    } else {
      log.trace("Kerberos ticket is valid");
    }
  }

  @Override
  public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
      final ClientHttpRequestExecution execution) throws IOException {

    PrivilegedAction<ClientHttpResponse> sendAction = () -> {
      try {
        if (log.isTraceEnabled()) {
          Subject current = Subject.getSubject(AccessController.getContext());
          log.trace("Executing HTTP Request. Principals: {}", current.getPrincipals());
        }

        ClientHttpResponse response = execution.execute(request, body);
        return response;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };

    checkTicketExpiration();

    ClientHttpResponse response = Subject.doAs(subject, sendAction);
    return response;

  }

}
