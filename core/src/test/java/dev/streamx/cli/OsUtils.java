package dev.streamx.cli;

import com.github.jknack.handlebars.internal.text.StringEscapeUtils;

public class OsUtils {
  public static final String ESCAPED_LINE_SEPARATOR =
      StringEscapeUtils.ESCAPE_JSON.translate(System.lineSeparator());
}
