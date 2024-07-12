package dev.streamx.cli.test.tools.terminal.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jboss.logging.Logger;

class StreamDataCollectorThread extends Thread {

  private final Logger logger = Logger.getLogger(StreamDataCollectorThread.class);
  private final CopyOnWriteArrayList<String> ongoingDataCollection;
  private final InputStream inputStream;
  private final String inputStreamDescription;

  StreamDataCollectorThread(
      InputStream inputStream,
      String inputStreamDescription,
      String name) {
    this.ongoingDataCollection = new CopyOnWriteArrayList<>();
    this.inputStreamDescription = inputStreamDescription;
    this.inputStream = inputStream;
    this.setDaemon(true);
    this.setName(name);
  }

  @Override
  public void run() {
    try (BufferedReader reader = createBufferedReader(inputStream)) {
      readLinesAndAddToCollectedData(reader);
    } catch (IOException e) {
      handleException(e);
    }
  }

  private void readLinesAndAddToCollectedData(BufferedReader reader) throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      ongoingDataCollection.add(line);
    }
  }

  private void handleException(IOException e) {
    if (e instanceof InterruptedIOException) {
      logger.debug("IO Interrupted for stream: " + inputStreamDescription);
    } else {
      throw new RuntimeException("IOException for stream: " + inputStreamDescription, e);
    }
  }

  private BufferedReader createBufferedReader(InputStream inputStream) {
    return new BufferedReader(new InputStreamReader(inputStream));
  }

  CopyOnWriteArrayList<String> getOngoingDataCollection() {
    return ongoingDataCollection;
  }
}
