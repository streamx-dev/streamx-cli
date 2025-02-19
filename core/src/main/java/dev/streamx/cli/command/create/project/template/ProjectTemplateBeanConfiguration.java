package dev.streamx.cli.command.create.project.template;

import static dev.streamx.cli.command.create.project.template.ProdProjectTemplateSource.PROD_PROJECT_TEMPLATE_SOURCE_URL;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class ProjectTemplateBeanConfiguration {

  public static final String REPO_URL = "streamx.cli.create.project.template.repo-url";

  @Produces
  @IfBuildProfile("prod")
  ProjectTemplateSource prodLicenseSource() {
    return new ProdProjectTemplateSource();
  }

  @Produces
  @DefaultBean
  ProjectTemplateSource configurableLicenseSource(
      @ConfigProperty(name = REPO_URL,
          defaultValue = PROD_PROJECT_TEMPLATE_SOURCE_URL)
      String url
  ) {
    return new ConfigurableProjectTemplateSource(url);
  }
}
