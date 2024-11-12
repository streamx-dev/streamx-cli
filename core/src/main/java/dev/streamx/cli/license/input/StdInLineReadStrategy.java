package dev.streamx.cli.license.input;

import static dev.streamx.cli.util.Output.print;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public class StdInLineReadStrategy implements AcceptingStrategy {

  @Override
  public boolean isLicenseAccepted() {
    try {
      do {
        CharSequence sb = readInput();
        if (isEmpty(sb)) {
          return true;
        } else if (containsSingleCharacter(sb)) {
          char ch = sb.charAt(0);
          if (isAccepted(ch)) {
            return true;
          } else if (isRejected(ch)) {
            return false;
          }
        }

        print("Do you accept the license agreement? [Y/n]");
      } while (true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  private static StringBuilder readInput() throws IOException {
    StringBuilder sb = new StringBuilder();
    char c;
    while ((c = ((char) System.in.read())) != '\n') {
      if (!Character.isWhitespace(c)) {
        sb.append(c);
      }
    }
    return sb;
  }

  private static boolean isEmpty(CharSequence sb) {
    return sb.chars().count() == 0;
  }

  private static boolean containsSingleCharacter(CharSequence c) {
    return c.chars().count() == 1;
  }

  private static boolean isAccepted(char ch) {
    return ch == 'y' || ch == 'Y';
  }

  private static boolean isRejected(char ch) {
    return ch == 'n' || ch == 'N';
  }
}
