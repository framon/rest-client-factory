package br.eti.monzeu.restclientfactory.auth.spnego;

import java.util.HashMap;
import java.util.Set;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

public class SPNegoCredentialsModuleConfig extends Configuration {

  private static final String JGSS_ACCEPT = "com.sun.security.jgss.accept";
  private static final String JGSS_INITIATE = "com.sun.security.jgss.initiate";
  private static final String JGSS_LOGIN = "com.sun.security.jgss.login";
  private static final String LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule";

  private Set<String> configsName = Set.of(JGSS_LOGIN, JGSS_INITIATE, JGSS_ACCEPT);
  private AppConfigurationEntry appConfigurationEntry;

  public SPNegoCredentialsModuleConfig() {
    HashMap<String, Object> options = new HashMap<String, Object>();
    options.put("client", "TRUE");
    options.put("useTicketCache", "false");
    appConfigurationEntry = new AppConfigurationEntry(LOGIN_MODULE, LoginModuleControlFlag.REQUIRED,
        options);
  }

  @Override
  public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
    if (configsName.contains(name)) {
      return new AppConfigurationEntry[]{appConfigurationEntry};
    }

    throw new IllegalArgumentException("Unexpected entry '" + name + "'");
  }
}
