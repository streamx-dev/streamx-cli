package dev.streamx.cli.test.tools.terminal;

import dev.streamx.cli.test.tools.terminal.process.ShellProcess;

public interface TerminalCommandRunner {

  ShellProcess run(String command);
}
