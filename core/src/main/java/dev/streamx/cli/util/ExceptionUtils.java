package dev.streamx.cli.util;

public final class ExceptionUtils {

  public static RuntimeException sneakyThrow(Throwable t) {
    if (t == null) {
      throw new NullPointerException("t");
    }
    return ExceptionUtils.<RuntimeException>sneakyThrow0(t);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
    throw (T) t;
  }
}
