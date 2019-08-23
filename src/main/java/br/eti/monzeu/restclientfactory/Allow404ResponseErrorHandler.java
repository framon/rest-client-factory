package br.eti.monzeu.restclientfactory;

import java.util.EnumSet;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class Allow404ResponseErrorHandler extends DefaultResponseErrorHandler {

  private static final EnumSet goodStatus = EnumSet.of(HttpStatus.OK, HttpStatus.NOT_FOUND);

  @Override
  protected boolean hasError(HttpStatus statusCode) {
    return !goodStatus.contains(statusCode) && super.hasError(statusCode);
  }
}
