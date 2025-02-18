package dev.streamx.cli.command.create.project.template;

public class ProdProjectTemplateSource implements ProjectTemplateSource {

  public static final String PROD_PROJECT_TEMPLATE_SOURCE_URL =
      "git@github.com:streamx-dev/streamx-project-template.git";

  @Override
  public String getRepoUrl() {
    return PROD_PROJECT_TEMPLATE_SOURCE_URL;
  }
}
