package dev.streamx.cli.command.create.project.template;

public class ConfigurableProjectTemplateSource implements ProjectTemplateSource {

  private final String projectTemplateUrl;

  public ConfigurableProjectTemplateSource(String projectTemplateUrl) {
    this.projectTemplateUrl = projectTemplateUrl;
  }

  @Override
  public String getRepoUrl() {
    return projectTemplateUrl;
  }
}
