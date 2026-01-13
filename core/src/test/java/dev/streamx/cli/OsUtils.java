package dev.streamx.cli;

import org.apache.commons.lang3.StringEscapeUtils;

public class OsUtils {
  public static final String ESCAPED_LINE_SEPARATOR =
      StringEscapeUtils.escapeJson(System.lineSeparator());
}
