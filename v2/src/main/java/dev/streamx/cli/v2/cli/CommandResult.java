package dev.streamx.cli.v2.cli;

import static dev.streamx.cli.v2.i18n.MessageProvider.msg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.util.Optional;
import java.util.function.Function;

/**
 * @param <ResultT> Must be serializable by Jackson (POJO, JsonSerializable, etc.)
 */
public class CommandResult<ResultT> {
  public ResultT result;

  public CommandResult(ResultT result) {
    this.result = result;
  }

  public Optional<String> toText(OutputFormat outputFormat, Function<CommandResult<ResultT>, Optional<String>> textFormatter) throws RuntimeException {
    try {
      switch (outputFormat) {
        case OutputFormat.text -> {
          return textFormatter.apply(this);
        }
        case OutputFormat.json -> {
          ObjectMapper mapper = new ObjectMapper();
          JsonNode jsonNode = mapper.valueToTree(result);
          return Optional.of(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
        }
        case OutputFormat.yaml -> {
          var yamlFactory = YAMLFactory.builder()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .build();
          ObjectMapper mapper = new ObjectMapper(yamlFactory);
          JsonNode jsonNode = mapper.valueToTree(result);
          return Optional.of(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode).strip());
        }
      }

      throw new RuntimeException(msg.unsupportedOutputFormat());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
