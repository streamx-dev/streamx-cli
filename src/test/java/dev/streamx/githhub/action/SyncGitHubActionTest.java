package dev.streamx.githhub.action;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.kohsuke.github.GHEventPayload;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncGitHubActionTest extends AbstractGitHubActionTest {

  private SyncGitHubAction action;
  @Mock
  private Logger logger;
  @Mock
  private GHEventPayload.PullRequest eventPayload;
  @Mock
  private Context context;
  @Mock
  private DataSourceProvider dataSourceProvider;

  @BeforeEach
  public void setUp() throws StreamxClientException {
    super.setUp();
    action = new SyncGitHubAction();
    action.log = logger;
    action.streamxClientProvider = streamxClientProvider;
    when(dataSourceProvider.getName()).thenReturn("sync_action_source_provider");
    action.dataSourceProviders = Collections.singletonList(dataSourceProvider);
    action.ingestionConfig = ingestionConfig;
    action.objectMapper = objectMapper;
  }

  @Test
  public void testShouldValidateRequiredInputParameters() {
    action.syncAction(commands, inputs, context, eventPayload);
    verify(commands, times(1)).error(
        "Missing required streamx-ingestion-url input parameter. StreamX ingestion skipped.");

    reset(commands);
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    action.syncAction(commands, inputs, context, eventPayload);
    verify(commands, times(1)).error(
        "Missing required channel input parameter. StreamX ingestion skipped.");

    reset(commands);
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(
        Optional.of("webresource"));
    action.syncAction(commands, inputs, context, eventPayload);
    verify(commands, times(1)).error(
        "Missing required source-provider input parameter. StreamX ingestion skipped.");

    reset(commands);
    mockInputParameters();
    action.syncAction(commands, inputs, context, eventPayload);
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
    when(dataSourceProvider.createPayload(inputs, context, eventPayload)).thenReturn(
        requestPayload);

    action.syncAction(commands, inputs, context, eventPayload);

    verify(streamxClient, times(1))
        .newPublisher("webresource", JsonNode.class);
    verify(publisher, times(1)).send(anyList());
  }

  @Test
  public void testShouldSendPublishMessageInPartitions() throws StreamxClientException,
      GitHubActionException {
    mockInputParameters();
    reset(ingestionConfig);
    when(ingestionConfig.batchSourceProviderBatchSizeInBytes()).thenReturn(150L);

    JsonNode message = IngestionPayloadJsonFactory.createMessage(
        "/test/streamx.key",
        Message.PUBLISH_ACTION,
        createTestPayloadContent("Test content"),
        null,
        "web-resource/static"
    );
    List<JsonNode> requestPayload = new ArrayList<>();
    requestPayload.add(message);
    requestPayload.add(message);
    requestPayload.add(message);
    when(dataSourceProvider.createPayload(inputs, context, eventPayload)).thenReturn(
        requestPayload);

    action.syncAction(commands, inputs, context, eventPayload);

    verify(streamxClient, times(1))
        .newPublisher("webresource", JsonNode.class);
    verify(publisher, times(3)).send(anyList());
  }

  @Test
  public void testShouldNotSendPublishMessageWhenSizeIsOverLimits() throws StreamxClientException,
      GitHubActionException {
    mockInputParameters();
    reset(ingestionConfig);
    when(ingestionConfig.batchSourceProviderBatchSizeInBytes()).thenReturn(150L);

    JsonNode message = IngestionPayloadJsonFactory.createMessage(
        "/test/streamx.key",
        Message.PUBLISH_ACTION,
        createTestPayloadContent("Test content"),
        null,
        "web-resource/static"
    );
    JsonNode inValidMessage = IngestionPayloadJsonFactory.createMessage(
        "/test/streamx2.key",
        Message.PUBLISH_ACTION,
        createTestPayloadContent("Test content with invalid size. Message should not get send."),
        null,
        "web-resource/static"
    );
    List<JsonNode> requestPayload = new ArrayList<>();
    requestPayload.add(message);
    requestPayload.add(inValidMessage);
    when(dataSourceProvider.createPayload(inputs, context, eventPayload)).thenReturn(
        requestPayload);

    action.syncAction(commands, inputs, context, eventPayload);

    verify(streamxClient, times(1))
        .newPublisher("webresource", JsonNode.class);
    verify(publisher, times(1)).send(anyList());
  }

  private void mockInputParameters() {
    mockBaseInputParameters();
    String page = "webresource";
    lenient().when(inputs.getRequired(Constants.INGESTION_CHANNEL)).thenReturn(page);
    lenient().when(inputs.get(Constants.INGESTION_CHANNEL)).thenReturn(
        Optional.of(page));

    String pageKey = "webresource_key";
    lenient().when(inputs.getRequired(Constants.INGESTION_MESSAGE_KEY)).thenReturn(pageKey);
    lenient().when(inputs.get(Constants.INGESTION_MESSAGE_KEY)).thenReturn(
        Optional.of(pageKey));

    String actionSourceProvider = "sync_action_source_provider";
    lenient().when(inputs.getRequired(Constants.INGESTION_SOURCE_PROVIDER))
        .thenReturn(actionSourceProvider);
    lenient().when(inputs.get(Constants.INGESTION_SOURCE_PROVIDER))
        .thenReturn(Optional.of(actionSourceProvider));
  }

}