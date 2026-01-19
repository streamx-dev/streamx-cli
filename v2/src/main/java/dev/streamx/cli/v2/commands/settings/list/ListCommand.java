package dev.streamx.cli.v2.commands.settings.list;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.streamx.cli.v2.cli.AbstractCommand;
import dev.streamx.cli.v2.cli.CommandResult;
import dev.streamx.cli.v2.commands.settings.SettingsFile;
import org.jboss.logging.Logger;
import picocli.CommandLine;

import java.net.URL;
import java.util.*;

@CommandLine.Command(
  name = "list",
  mixinStandardHelpOptions = true,
  description = "Display configuration properties"
)
public class ListCommand extends AbstractCommand {
  private static final Logger logger = Logger.getLogger(ListCommand.class);

  @Override
  public CommandResult runCommand() throws RuntimeException {
    var url = SettingsFile.getUrl();
    var properties = getProperties(url);

    return new CommandResult(
      Optional.of(convertToText(properties)),
      Optional.of(convertToJson(properties).toString())
    );
  }

  private HashMap<String, String> getProperties(URL url) throws RuntimeException {
    try (var input = url.openStream()) {
      Properties properties = new Properties();
      properties.load(input);

      HashMap<String, String> propertyMap = new HashMap<>();
      for (String key : properties.stringPropertyNames()) {
        propertyMap.put(key, properties.getProperty(key));
      }

      return propertyMap;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load properties from " + url, e);
    }
  }

  public static JsonNode convertToJson(HashMap<String, String> map) {
    ObjectMapper mapper = new ObjectMapper();
    ArrayNode arrayNode = mapper.createArrayNode();

    for (Map.Entry<String, String> entry : map.entrySet()) {
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("key", entry.getKey());
      objectNode.put("value", entry.getValue());
      arrayNode.add(objectNode);
    }

    return arrayNode;
  }

  private String convertToText(HashMap<String, String> properties) {
    StringBuilder result = new StringBuilder();
    Map<String, String> sortedProperties = new TreeMap<>(properties);

    int maxKeyLength = sortedProperties.keySet().stream()
      .mapToInt(String::length)
      .max()
      .orElse(0);

    result.append("\nConfiguration properties:\n");
    String repeat = "=".repeat(Math.min(80, maxKeyLength + 40));
    result.append(repeat).append("\n");

    for (Map.Entry<String, String> entry : sortedProperties.entrySet()) {
      String paddedKey = String.format("%-" + maxKeyLength + "s", entry.getKey());
      result.append(paddedKey).append(" = ").append(entry.getValue()).append("\n");
    }

    result.append(repeat).append("\n");
    result.append("Total properties: ").append(properties.size()).append("\n");

    return result.toString();
  }
}
