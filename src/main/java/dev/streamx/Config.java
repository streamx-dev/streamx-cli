package dev.streamx;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

@Dependent
public class Config {

  @ApplicationScoped
  public CloseableHttpClient httpClient() {
    return HttpClients.createDefault();
  }

  @ApplicationScoped
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

}
