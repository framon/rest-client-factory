package br.eti.monzeu.restclientfactory.auth.spnego;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

class UsernamePasswordCallBackHandler implements CallbackHandler {

  private final String username;
  private final String password;

  public UsernamePasswordCallBackHandler(String username, String password) {
    this.username = username;
    this.password = password;
  }

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

    for (Callback callback : callbacks) {

      if (callback instanceof NameCallback) {
        NameCallback nc = (NameCallback) callback;
        nc.setName(username);
      } else if (callback instanceof PasswordCallback) {
        PasswordCallback pc = (PasswordCallback) callback;
        pc.setPassword(password.toCharArray());
      } else {
        throw new UnsupportedCallbackException(callback, "Unknown Callback");
      }

    }
  }
}