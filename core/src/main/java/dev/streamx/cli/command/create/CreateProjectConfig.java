package dev.streamx.cli.command.create;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping
public interface CreateProjectConfig {

  String STREAMX_CREATE_PROJECT_TEMPLATE_OUTPUT_DIR =
      "streamx.cli.create.project.template.output-dir";

  @WithName(STREAMX_CREATE_PROJECT_TEMPLATE_OUTPUT_DIR)
  @WithDefault("streamx-project-template")
  String outputDir();
}
