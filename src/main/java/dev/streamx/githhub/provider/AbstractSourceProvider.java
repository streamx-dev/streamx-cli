package dev.streamx.githhub.provider;

import dev.streamx.exception.MissingRequiredInputException;
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

}
