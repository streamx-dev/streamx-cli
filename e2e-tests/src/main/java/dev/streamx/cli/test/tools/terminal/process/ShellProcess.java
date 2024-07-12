package dev.streamx.cli.test.tools.terminal.process;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class ShellProcess {

  private final Process process;
  private final StreamDataCollectorThread outputThread;
  private final StreamDataCollectorThread errorOutputThread;

  private ShellProcess(
      Process process,
      StreamDataCollectorThread outputThread,
      StreamDataCollectorThread errorOutputThread
  ) {
    this.process = process;
    this.outputThread = outputThread;
    this.errorOutputThread = errorOutputThread;
  }

  public List<String> getCurrentOutputLines() {
    return outputThread.getOngoingDataCollection();
  }

  public List<String> getCurrentErrorLines() {
    return errorOutputThread.getOngoingDataCollection();
  }

  public long pid() {
    return process.pid();
  }

  public void passInput(String input) {
    try (BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(process.getOutputStream()))) {
      writer.write(input);
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void destroy() {
    process.destroy();
    outputThread.interrupt();
    errorOutputThread.interrupt();
  }

  public static ShellProcess run(ProcessBuilder processBuilder) throws IOException {
    Process process = processBuilder.start();
    StreamDataCollectorThread outputThread = new StreamDataCollectorThread(
        process.getInputStream(),
        "Output stream for command:" + process.info().commandLine(),
        "OutputStreamDataCollectorForProcessPid-" + process.pid());
    outputThread.start();
    StreamDataCollectorThread errorThread = new StreamDataCollectorThread(
        process.getErrorStream(),
        "Error stream for command:" + process.info().commandLine(),
        "ErrorStreamDataCollectorForProcessPid-" + process.pid());
    errorThread.start();
    return new ShellProcess(process, outputThread, errorThread);
  }
}
