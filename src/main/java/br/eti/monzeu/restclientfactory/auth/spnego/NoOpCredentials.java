package br.eti.monzeu.restclientfactory.auth.spnego;

import java.security.Principal;
import org.apache.http.auth.Credentials;

final class NoOpCredentials implements Credentials {

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public Principal getUserPrincipal() {
    return null;
  }
}