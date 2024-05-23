package dev.streamx.cli;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class StreamxCommandProducer {

  private static final String STREAMX_JAR_PATTER = ".*/streamx-cli-.*-runner\\.jar";
  private static final String JAR_DIR_FOR_INTELLIJ = "../distribution/target/";
  private static final String JAR_DIR_FOR_MAVEN = "./distribution/target/";

  @ConfigProperty(name = "streamxCommandType", defaultValue = "built")
  String streamxCommandType;

  public StreamxCommand produce() {
    String command =
        streamxCommandType.equalsIgnoreCase("installed")
            ? installed()
            : built();
    return new StreamxCommand(command);
  }

  public String built() {
    return "java -jar " + findStreamxJar();
  }

  private String findStreamxJar() {
    Pattern pattern = Pattern.compile(STREAMX_JAR_PATTER);
    return Stream.of(JAR_DIR_FOR_INTELLIJ, JAR_DIR_FOR_MAVEN)
        .map(File::new)
        .filter(File::exists)
        .map(File::listFiles)
        .flatMap(Stream::of)
        .map(File::getAbsolutePath)
        .filter(p -> pattern.matcher(p).matches())
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Could not find streamx Jar"));
  }

  public String installed() {
    return "streamx";
  }
}
