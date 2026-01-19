package dev.streamx.cli.v2.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * @param <ResultT> Must be serializable by Jackson (POJO, JsonSerializable, etc.)
 */
public class CommandResult<ResultT> {
  public ResultT result;

  public CommandResult(ResultT result) {
    this.result = result;
  }

  public void print(OutputFormat outputFormat) throws Exception {
    if (outputFormat == OutputFormat.json) {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.valueToTree(result);
      System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
      return;
    }

    System.out.println(outputFormat);
    if (outputFormat == OutputFormat.yaml) {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      JsonNode jsonNode = mapper.valueToTree(result);
      System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
    }
  }
}
