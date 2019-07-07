package br.eti.monzeu.restclientfactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

  final static Logger logger = LoggerFactory.getLogger(LoggingRequestInterceptor.class);

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {

    traceRequest(request, body);
    ClientHttpResponse response = execution.execute(request, body);
    traceResponse(response);
    return response;
  }

  private void traceRequest(HttpRequest request, byte[] body) throws IOException {
    logger.info("\n---[HTTP request]---\nURI: {}\nMethod: {}\nBody: {}\n--------------------",
        request.getURI(), request.getMethod(), new String(body, "UTF-8"));
  }

  private void traceResponse(ClientHttpResponse response) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));

    for (String line = br.readLine(); line != null; line = br.readLine()) {
      sb.append(line).append('\n');
    }

    logger.info("\n---[HTTP response {}]---\n{}\n--------------------",
        response.getStatusCode(), sb);
  }

}