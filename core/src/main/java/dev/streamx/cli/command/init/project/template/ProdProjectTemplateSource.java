package dev.streamx.cli.command.init.project.template;

public class ProdProjectTemplateSource implements ProjectTemplateSource {

  public static final String PROD_PROJECT_TEMPLATE_SOURCE_URL =
      "https://github.com/streamx-dev/streamx-sample-project.git";

  @Override
  public String getRepoUrl() {
    return PROD_PROJECT_TEMPLATE_SOURCE_URL;
  }
}
