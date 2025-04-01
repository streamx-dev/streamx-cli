package dev.streamx.cli.command.init;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
public interface InitProjectConfig {

  String STREAMX_INIT_PROJECT_TEMPLATE_OUTPUT_DIR =
      "streamx.cli.init.project.template.output-dir";

  @WithName(STREAMX_INIT_PROJECT_TEMPLATE_OUTPUT_DIR)
  @WithDefault("streamx-sample-project")
  String outputDir();
}
