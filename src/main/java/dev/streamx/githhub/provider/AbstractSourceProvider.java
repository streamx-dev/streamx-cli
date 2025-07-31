package dev.streamx.githhub.provider;

import dev.streamx.exception.GitHubActionException;
import dev.streamx.exception.MissingRequiredInputException;
import dev.streamx.githhub.Constants;
import dev.streamx.ingestion.schema.SchemaProvider;
import io.quarkiverse.githubaction.Inputs;
import java.util.Optional;

public abstract class AbstractSourceProvider implements DataSourceProvider {

  public static final String MISSING_INPUT_PARAMETER_ERR_MSG_FMT =
      "Provider is missing required input parameter: %s";

  protected void assertProviderRequiredInputParameters(Inputs inputs, String... inputNames)
      throws MissingRequiredInputException {
    for (String inputName : inputNames) {
      Optional<String> inputOpt = inputs.get(inputName);
      if (inputOpt.isEmpty()) {
        throw new MissingRequiredInputException(
            String.format(MISSING_INPUT_PARAMETER_ERR_MSG_FMT, inputName));
      }
    }
  }

  protected String getIngestionSchemaType(SchemaProvider schemaProvider, Inputs inputs)
      throws GitHubActionException {
    String url = getInputString(inputs, Constants.STREAMX_INGESTION_URL);
    String token = getInputString(inputs, Constants.STREAMX_INGESTION_TOKEN);
    String channel = getInputString(inputs, Constants.INGESTION_CHANNEL);
    return schemaProvider.getSchemaType(url, token, channel);
  }

}
