package dev.streamx.cli;

import java.io.File;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class StreamxTerminalCommandProducer {

  private static final String STREAMX_JAR_PATTER = ".*/streamx-cli-.*-runner\\.jar";
  private static final String JAR_DIR_FOR_INTELLIJ = "../distribution/target/";
  private static final String JAR_DIR_FOR_MAVEN = "./distribution/target/";

  private final String streamxCommandType;

  public StreamxTerminalCommandProducer(String streamxCommandType) {
    this.streamxCommandType = streamxCommandType;
  }

  public StreamxTerminalCommand produce() {
    String command =
        streamxCommandType.equalsIgnoreCase("installed")
            ? installed()
            : built();
    return new StreamxTerminalCommand(command);
  }

  public String built() {
    return "java %s -jar %s".formatted(createParameters(), findStreamxJar());
  }

  private static String createParameters() {
    return "-Dstreamx.runner.observability.enabled=false";
  }

  private String findStreamxJar() {
    Pattern pattern = Pattern.compile(STREAMX_JAR_PATTER);
    return Stream.of(JAR_DIR_FOR_INTELLIJ, JAR_DIR_FOR_MAVEN)
        .map(File::new)
        .filter(File::exists)
        .map(File::listFiles)
        .flatMap(Stream::of)
        .map(File::getAbsolutePath)
        .filter(p -> pattern.matcher(p.replace("\\", "/")).matches())
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Could not find streamx Jar"));
  }

  public String installed() {
    return "streamx";
  }
}
