package dev.streamx.cli.exception;

import static dev.streamx.cli.command.cloud.MetadataUtils.SERVICEMESH_CRD_NAME;

import dev.streamx.cli.util.ExceptionUtils;
import io.fabric8.kubernetes.client.KubernetesClientException;

public class KubernetesException extends RuntimeException {

  private static final String MISSING_CRD_MESSAGE = """
      The required CustomResourceDefinition (CRD) "%s" is not installed on the cluster.
                
      Make sure that:
      * You are connected to the correct Kubernetes cluster. You can verify your current cluster \
      context by running:
            
          kubectl config current-context
            
      * StreamX Operator Custom Resource Definitions are installed on the cluster.""".formatted(
      SERVICEMESH_CRD_NAME);
  private static final String K8S_CLIENT_CONNECTION_EXCEPTION_MESSAGE = """
      Encountered an error while attempting to communicate with the cluster:
      %s
            
      Make sure that:
      * Your kubeconfig file is properly configured. You can verify the setup by running:
            
          kubectl config view
            
      * You are connected to the correct Kubernetes cluster. You can verify your current \
      cluster context by running:
            
          kubectl config current-context
            
      * The kubeconfig file is accessible and points to a valid cluster configuration.""";
  public static final String K8S_CLIENT_METHOD_EXCEPTION_MESSAGE
      = "Error updating resource %s [%s] in namespace '%s': HTTP %d (%s)";

  private KubernetesException(String message, Exception exception) {
    super(message, exception);
  }

  private KubernetesException(String message) {
    super(message);
  }

  public static KubernetesException serviceMeshCrdNotFound() {
    return new KubernetesException(MISSING_CRD_MESSAGE);
  }

  public static KubernetesException kubernetesClientException(KubernetesClientException e) {
    if (e.getStatus() != null) {
      // We are able to communicate with the cluster, Kubernetes returned error response
      return new KubernetesException(
          ExceptionUtils.appendLogSuggestion(K8S_CLIENT_METHOD_EXCEPTION_MESSAGE.formatted(
              e.getName(), e.getResourcePlural(), e.getNamespace(), e.getCode(),
              e.getStatus().getReason())),
          e);
    }
    return new KubernetesException(
        ExceptionUtils.appendLogSuggestion(K8S_CLIENT_CONNECTION_EXCEPTION_MESSAGE.formatted(
            e.getMessage())), e);
  }
}
