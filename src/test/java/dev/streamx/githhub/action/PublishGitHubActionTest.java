package dev.streamx.githhub.action;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import dev.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.clients.ingestion.publisher.Message;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.githhub.Constants;
import dev.streamx.githhub.provider.DataSourceProvider;
import dev.streamx.ingestion.IngestionPayloadJsonFactory;
import io.quarkiverse.githubaction.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublishGitHubActionTest extends AbstractGitHubActionTest {

  private PublishGitHubAction action;

  @Mock
  private Logger logger;
  @Mock
  private Context context;
  @Mock
  private DataSourceProvider dataSourceProvider;

  @BeforeEach
  public void setUp() throws StreamxClientException {
    super.setUp();
    action = new PublishGitHubAction();
    action.log = logger;
    action.streamxClientProvider = streamxClientProvider;
    when(dataSourceProvider.getName()).thenReturn("action_source_provider");
    action.dataSourceProviders = Collections.singletonList(dataSourceProvider);
    action.ingestionConfig = ingestionConfig;
    action.objectMapper = objectMapper;
  }

  @Test
  public void testShouldValidateRequiredInputParameters() throws GitHubActionException {
    assertThrows(MissingRequiredInputException.class,
        () -> action.publishAction(commands, inputs, context));
    verify(commands, times(1)).error(
        "Missing required streamx-ingestion-url input parameter. StreamX ingestion skipped.");

    reset(commands);
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    assertThrows(MissingRequiredInputException.class,
        () -> action.publishAction(commands, inputs, context));
    verify(commands, times(1)).error(
        "Missing required channel input parameter. StreamX ingestion skipped.");

    reset(commands);
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(
        Optional.of("page"));
    assertThrows(MissingRequiredInputException.class,
        () -> action.publishAction(commands, inputs, context));
    verify(commands, times(1)).error(
        "Missing required source-provider input parameter. StreamX ingestion skipped.");

    reset(commands);
    mockInputParameters();
    action.publishAction(commands, inputs, context);
    verify(commands, never()).error(anyString());
  }

  @Test
  public void testShouldSendPublishMessage() throws StreamxClientException,
      GitHubActionException {
    mockInputParameters();

    JsonNode message = IngestionPayloadJsonFactory.createMessage(
        "/test/streamx.key",
        Message.PUBLISH_ACTION,
        createTestPayloadContent("Test content"),
        null,
        "web-resource/static"
    );
    List<JsonNode> requestPayload = new ArrayList<>();
    requestPayload.add(message);
    when(dataSourceProvider.createPayload(inputs, context, null))
        .thenReturn(requestPayload);

    action.publishAction(commands, inputs, context);

    verify(streamxClient, times(1))
        .newPublisher("page", JsonNode.class);
    verify(publisher, times(1)).send(eq(requestPayload));
  }

  private void mockInputParameters() {
    mockBaseInputParameters();
    String page = "page";
    lenient().when(inputs.getRequired(Constants.INGESTION_CHANNEL)).thenReturn(page);
    lenient().when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(Optional.of(page));

    String pageKey = "page_key";
    lenient().when(inputs.getRequired(Constants.INGESTION_MESSAGE_KEY)).thenReturn(pageKey);
    lenient().when(inputs.get(Constants.INGESTION_MESSAGE_KEY)).thenReturn(Optional.of(pageKey));

    String actionSourceProvider = "action_source_provider";
    lenient().when(inputs.getRequired(Constants.INGESTION_SOURCE_PROVIDER))
        .thenReturn(actionSourceProvider);
    lenient().when(inputs.get(Constants.INGESTION_SOURCE_PROVIDER))
        .thenReturn(Optional.of(actionSourceProvider));
  }
}