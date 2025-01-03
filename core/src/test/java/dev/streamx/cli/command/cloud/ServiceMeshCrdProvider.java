package dev.streamx.cli.command.cloud;

import dev.streamx.cli.command.cloud.deploy.DeployCommandIT;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
@IfBuildProperty(name = "%test.quarkus.kubernetes-client.devservices.enabled",
    stringValue = "true")
public class ServiceMeshCrdProvider {

  @Inject
  KubernetesClient kubernetesClient;

  void onStart(@Observes StartupEvent ev) {
    kubernetesClient.apiextensions().v1().customResourceDefinitions()
        .load(DeployCommandIT.class.getResourceAsStream("servicemeshes.streamx.dev-v1.yml"))
        .createOr(NonDeletingOperation::update);
  }
}
