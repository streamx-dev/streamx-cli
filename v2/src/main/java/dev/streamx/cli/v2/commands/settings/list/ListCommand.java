//package dev.streamx.cli.v2.commands.settings.list;
//
//import dev.streamx.cli.v2.commands.settings.SettingsCommand;
//import dev.streamx.cli.v2.commands.settings.SettingsFile;
//import org.jboss.logging.Logger;
//import picocli.CommandLine;
//
//import java.net.URL;
//import java.util.*;
//
//@CommandLine.Command(
//  name = "list",
//  mixinStandardHelpOptions = true,
//  description = "Display configuration properties"
//)
//public class ListCommand implements Runnable {
//  private static final Logger logger = Logger.getLogger(ListCommand.class);
//
//  @Override
//  public void run() throws RuntimeException {
//    printProperties();
//  }
//
//  private Either<RuntimeException, HashMap<String, String>> getProperties(URL url) {
//    return Try.withResources(url::openStream)
//      .of(input -> {
//        Properties properties = new Properties();
//        properties.load(input);
//
//        HashMap<String, String> propertyMap = new HashMap<>();
//        for (String key : properties.stringPropertyNames()) {
//          propertyMap.put(key, properties.getProperty(key));
//        }
//
//        return propertyMap;
//      })
//      .toEither().mapLeft(e -> new RuntimeException("Failed to load properties from " + url, e));
//  }
//
//  private Either<RuntimeException, Void> printProperties() {
//    return SettingsFile.getUrl()
//      .flatMap(this::getProperties)
//      .flatMap(properties -> {
//        Map<String, String> sortedProperties = new TreeMap<>(properties);
//
//        int maxKeyLength = sortedProperties.keySet().stream()
//          .mapToInt(String::length)
//          .max()
//          .orElse(0);
//
//        logger.info("\nConfiguration properties:");
//        String repeat = "=".repeat(Math.min(80, maxKeyLength + 40));
//        logger.info(repeat);
//
//        sortedProperties.forEach((key, value) -> {
//          String paddedKey = String.format("%-" + maxKeyLength + "s", key);
//          logger.info(paddedKey + " = " + value);
//        });
//
//        logger.info(repeat);
//        logger.info("Total properties: " + properties.size());
//
//        return Either.right(null);
//      });
//  }
//}
