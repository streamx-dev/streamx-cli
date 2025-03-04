package dev.streamx.cli.command.dev;

import java.util.List;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class DashboardContainer extends GenericContainer<DashboardContainer> {

  public static final String CONTAINER_NAME = "streamx-dashboard";

  private static final int DASHBOARDS_CONTAINER_PORT = 8080;

  public DashboardContainer(String fullImageName, int exposedPort, String meshPath,
      String projectDirectory) {
    super(DockerImageName.parse(fullImageName));
    setExposedPorts(List.of(DASHBOARDS_CONTAINER_PORT));
    addFixedExposedPort(exposedPort, DASHBOARDS_CONTAINER_PORT);

    withFileSystemBind(projectDirectory, "/data/project", BindMode.READ_WRITE);
    withFileSystemBind(meshPath, "/data/mesh.yaml", BindMode.READ_WRITE);

    withCreateContainerCmdModifier(cmd -> cmd.withName(CONTAINER_NAME));
  }
}
