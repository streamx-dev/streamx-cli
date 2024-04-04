package dev.streamx.cli.run.util;

import static org.apache.http.HttpStatus.SC_OK;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.streamx.cli.util.ExceptionUtils;
import dev.streamx.runner.event.MeshStarted;
import io.quarkus.runtime.ApplicationLifecycleManager;
import io.quarkus.runtime.Quarkus;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.awaitility.Awaitility;

public class StreamxRunManager {

  private static final String STREAMX_RUNNER_MANAGER_PORT = "streamx.runner.manager.port";

  private final QuarkusMainLauncher launcher;
  private final String[] args;
  private final AtomicReference<LaunchResult> atomicReference = new AtomicReference<>();
  private final HttpClient httpClient = HttpClient.newBuilder().build();
  private HttpRequest readyRequest;
  private HttpRequest shutdownRequest;

  public static StreamxRunManager of(QuarkusMainLauncher launcher, String... args) {
    return new StreamxRunManager(launcher, args);
  }

  private StreamxRunManager(QuarkusMainLauncher launcher, String... args) {
    this.launcher = launcher;
    this.args = args;
  }

  public void start() {
    int port = findRandomPort();

    System.setProperty(STREAMX_RUNNER_MANAGER_PORT, Integer.toString(port));
    initializeRequests(port);

    Thread streamxRun = new Thread(() -> {
      LaunchResult result = launcher.launch(this.args);

      atomicReference.set(result);
    });
    streamxRun.start();
    awaitMeshStarted();
  }

  private void awaitMeshStarted() {
    Awaitility.await().pollInterval(1, TimeUnit.SECONDS)
        .atMost(1, TimeUnit.MINUTES)
        .until(() -> {
          try {
            return httpClient.send(readyRequest, BodyHandlers.discarding()).statusCode() == 200;
          } catch (Exception e) {
            return false;
          }
        });
  }

  private void initializeRequests(int port) {
    try {
      readyRequest = HttpRequest.newBuilder()
          .uri(new URI("http://localhost:%d/ready".formatted(port)))
          .GET()
          .build();
      shutdownRequest = HttpRequest.newBuilder()
          .uri(new URI("http://localhost:%d/shutdown".formatted(port)))
          .GET()
          .build();
    } catch (URISyntaxException e) {
      throw ExceptionUtils.sneakyThrow(e);
    }
  }

  private static int findRandomPort() {
    int port;
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      port = serverSocket.getLocalPort();
    } catch (IOException e) {
      throw ExceptionUtils.sneakyThrow(e);
    }
    return port;
  }

  public void shutdown() {
    Awaitility.await().pollInterval(1, TimeUnit.SECONDS)
        .atMost(1, TimeUnit.MINUTES)
        .until(() -> {
          try {
            HttpResponse<Void> response = httpClient.send(shutdownRequest, BodyHandlers.discarding());

            return response.statusCode() == SC_OK;
          } catch (Exception e) {
            return false;
          }
        });
  }

  public LaunchResult fetchResult() {
    Awaitility.await().pollInterval(1, TimeUnit.SECONDS)
        .atMost(1, TimeUnit.MINUTES)
        .until(() -> {
          LaunchResult launchResult = atomicReference.get();

          return launchResult != null;
        });

    return atomicReference.get();
  }

  public static class StreamxRunTestProfile implements QuarkusTestProfile {
    @Override
    public Set<Class<?>> getEnabledAlternatives() {
      return Set.of(Listener.class);
    }

    @Alternative
    static class Listener {

      void onMeshStarted(@Observes MeshStarted event) {
        Integer port = Integer.getInteger(STREAMX_RUNNER_MANAGER_PORT);
        HttpServer server;
        try {
          server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
          server.createContext("/ready", Listener::okResponse);
          server.createContext("/shutdown", Listener::shutdown);
          server.start();
          System.out.printf(" Server started on port %d%n", port);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      private static void shutdown(HttpExchange exchange) throws IOException {
        ApplicationLifecycleManager.exit();
        Quarkus.blockingExit();

        okResponse(exchange);
      }

      private static void okResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(SC_OK, 0);
        exchange.getResponseBody().close();
      }
    }
  }
}
