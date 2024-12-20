package dev.streamx.cli.command.manager;

import java.util.List;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class MeshManagerContainer extends GenericContainer<MeshManagerContainer> {

  static final String CONTAINER_NAME = "streamx-mesh-manager";

  private static final int MESH_MANAGER_CONTAINER_PORT = 8080;

  MeshManagerContainer(String fullImageName, int exposedPort, String meshPath,
      String projectDirectory) {
    super(DockerImageName.parse(fullImageName));
    setExposedPorts(List.of(MESH_MANAGER_CONTAINER_PORT));
    addFixedExposedPort(exposedPort, MESH_MANAGER_CONTAINER_PORT);

    withFileSystemBind(projectDirectory, "/data/project", BindMode.READ_WRITE);
    withFileSystemBind(meshPath, "/data/mesh.yaml", BindMode.READ_WRITE);

    withCreateContainerCmdModifier(cmd -> cmd.withName(CONTAINER_NAME));
  }
}
