package dev.streamx.githhub.action;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.streamx.clients.ingestion.exceptions.StreamxClientException;
import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.githhub.Constants;
import io.cloudevents.CloudEvent;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UnpublishGitHubActionTest extends AbstractGitHubActionTest {

  private UnpublishGitHubAction action;

  @BeforeEach
  public void setUp() throws StreamxClientException {
    super.setUp();
    action = new UnpublishGitHubAction();
    action.streamxClientProvider = streamxClientProvider;
  }

  @Test
  public void testShouldValidateRequiredInputParameters() throws GitHubActionException {
    assertThrows(MissingRequiredInputException.class,
        () -> action.unpublishAction(commands, inputs));
    verify(commands, times(1)).error(
        "Missing required streamx-ingestion-url input parameter. StreamX ingestion skipped.");

    reset(commands);
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    assertThrows(MissingRequiredInputException.class,
        () -> action.unpublishAction(commands, inputs));
    verify(commands, times(1)).error(
        "Missing required unpublish-event-type input parameter. StreamX ingestion skipped.");

    reset(commands);
    when(inputs.get(Constants.STREAMX_INGESTION_URL)).thenReturn(
        Optional.of("https://ingestion.streamx.dev"));
    when(inputs.get(Constants.UNPUBLISH_EVENT_TYPE)).thenReturn(
        Optional.of("com.streamx.blueprints.web-resource.unpublished.v1"));
    assertThrows(MissingRequiredInputException.class,
        () -> action.unpublishAction(commands, inputs));
    verify(commands, times(1)).error(
        "Missing required key input parameter. StreamX ingestion skipped.");

    reset(commands);
    mockInputParameters();
    action.unpublishAction(commands, inputs);
    verify(commands, never()).error(anyString());
  }

  @Test
  public void testShouldSendUnpublishMessage() throws StreamxClientException,
      GitHubActionException {
    mockInputParameters();

    action.unpublishAction(commands, inputs);

    verify(streamxClient, times(1)).newPublisher();
    verify(publisher, times(1)).send(argThat((CloudEvent event) ->
        "page_key".equals(event.getSubject())));
  }


  private void mockInputParameters() {
    mockBaseInputParameters();

    String eventType = "com.streamx.blueprints.web-resource.unpublished.v1";
    lenient().when(inputs.getRequired(Constants.UNPUBLISH_EVENT_TYPE)).thenReturn(eventType);
    lenient().when(inputs.get(Constants.UNPUBLISH_EVENT_TYPE)).thenReturn(Optional.of(eventType));

    String pageKey = "page_key";
    lenient().when(inputs.getRequired(Constants.INGESTION_MESSAGE_KEY)).thenReturn(pageKey);
    lenient().when(inputs.get(Constants.INGESTION_MESSAGE_KEY)).thenReturn(Optional.of(pageKey));
  }
}
