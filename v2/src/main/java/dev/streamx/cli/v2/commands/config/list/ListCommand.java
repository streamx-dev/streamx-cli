package dev.streamx.cli.v2.commands.config.list;

import dev.streamx.cli.v2.commands.config.ConfigFile;
import io.vavr.control.Try;
import org.jboss.logging.Logger;
import picocli.CommandLine;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

@CommandLine.Command(
  name = "list",
  mixinStandardHelpOptions = true,
  description = "Display configuration properties"
)
public class ListCommand implements Runnable {
  private static final Logger logger = Logger.getLogger(ListCommand.class);

  private HashMap<String, String> getProperties(URL url) {
    return Try.withResources(url::openStream)
      .of(input -> {
        Properties properties = new Properties();
        properties.load(input);

        HashMap<String, String> propertyMap = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
          propertyMap.put(key, properties.getProperty(key));
        }

        return propertyMap;
      })
      .onFailure(e -> {
        logger.error("Failed to load properties from " + url);
        logger.debug(e);
      })
      .getOrElse(new HashMap<>());
  }

  private void printProperties(HashMap<String, String> properties) {
    Map<String, String> sortedProperties = new TreeMap<>(properties);

    int maxKeyLength = sortedProperties.keySet().stream()
      .mapToInt(String::length)
      .max()
      .orElse(0);

    System.out.println("\nConfiguration properties:");
    String repeat = "=".repeat(Math.min(80, maxKeyLength + 40));
    System.out.println(repeat);

    sortedProperties.forEach((key, value) -> {
      String paddedKey = String.format("%-" + maxKeyLength + "s", key);
      System.out.println(paddedKey + " = " + value);
    });

    System.out.println(repeat);
    System.out.println("Total properties: " + properties.size());
  }

  @Override
  public void run() {
    ConfigFile.getUrl().fold(
      err -> {
        logger.error(err);
        System.exit(1);
        return null;
      },
      url -> {
        printProperties(getProperties(url));
        return null;
      }
    );
  }
}
