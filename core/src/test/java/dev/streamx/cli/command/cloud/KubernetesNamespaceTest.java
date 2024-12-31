package dev.streamx.cli.command.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KubernetesNamespaceTest {

  @Test
  void shouldReturnDefaultNamespace() {
    assertEquals("default", KubernetesNamespace.getNamespace(null));
  }

  @Test
  void shouldReturnCustomNamespace() {
    KubernetesNamespace kubernetesNamespace = new KubernetesNamespace();
    String expectedNamespace = "custom";
    kubernetesNamespace.namespace = expectedNamespace;
    assertEquals(expectedNamespace, KubernetesNamespace.getNamespace(kubernetesNamespace));
  }
}