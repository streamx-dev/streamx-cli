package dev.streamx.cli.run;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import dev.streamx.runner.model.AbstractService;
import dev.streamx.runner.model.ServiceMesh;
import org.testcontainers.DockerClientFactory;

public class ImagePuller {

  private final DockerClient dockerClient = DockerClientFactory.lazyClient();
  private ServiceMesh mesh;

  void pullImages(ServiceMesh serviceMesh) {
    this.mesh = serviceMesh;
    dockerClient.authConfig(); // Fail fast
    serviceMesh.getDelivery().values().forEach(this::doPullImage);
    serviceMesh.getProcessing().values().forEach(this::doPullImage);
  }

  private void doPullImage(AbstractService s) {
    try {
      // Calculation of the image version should be used here. It's already implemented somewhere in the Runner
      dockerClient.pullImageCmd(
              mesh.getDefaultRegistry() + "/" + s.getImage() + ":" + mesh.getDefaultImageTag())
          .exec(new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
              if (item.getProgressDetail() == null) {
                return;
              }
              //System.out.print("\033[2K");
              System.out.print("\rPulling: " + s.getImage() + " ");
              System.out.print(
                  item.getProgressDetail().getCurrent() + " / " + item.getProgressDetail()
                      .getTotal());
            }
          })// See LoggedPullImageResultCallback
          .awaitCompletion(); // Should probably be done in parallel
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (com.github.dockerjava.api.exception.DockerClientException ex) {
      // This is strange exception raised on pull complete ...
    }
  }

}
