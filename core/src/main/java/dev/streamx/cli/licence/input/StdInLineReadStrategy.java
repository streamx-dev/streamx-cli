package dev.streamx.cli.licence.input;

import java.io.IOException;

public class StdInLineReadStrategy implements AcceptingStrategy {

  @Override
  public boolean isLicenceAccepted() {
    try {
      do {
        char c = (char) System.in.read();
        if (c == 'y' || c == 'Y') {
          return true;
        } else if (c == 'n' || c == 'N') {
          return false;
        }
      } while (true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
