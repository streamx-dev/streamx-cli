package dev.streamx.cli.v2.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.Optional;

public class CommandResult {
  public Optional<String> text;
  public Optional<JsonNode> json;

  public static CommandResult empty() {
    return new CommandResult(Optional.empty(), Optional.empty());
  }

  public CommandResult(Optional<String> text, Optional<JsonNode> json) {
    this.text = text;
    this.json = json;
  }

  public void print(OutputFormat outputFormat) throws Exception {
    if (outputFormat == OutputFormat.text && text.isPresent()) {
      System.out.println(text.get());
      return;
    }

    if (outputFormat == OutputFormat.json) {
      if (json.isEmpty()) {
        throw new RuntimeException("This command did not return any JSON output.");
      }

      ObjectMapper mapper = new ObjectMapper();
      System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json.get()));
      return;
    }

    if (outputFormat == OutputFormat.yaml) {
      if (json.isEmpty()) {
        throw new RuntimeException("This command did not return any YAML output.");
      }

      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json.get()));
    }
  }
}
