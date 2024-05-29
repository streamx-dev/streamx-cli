package dev.streamx.cli.license.input;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.mockito.Mockito;

@Dependent
public class LicenseInputConfig {

  @Singleton
  @IfBuildProfile("test")
  AcceptingStrategy mockedAcceptingStrategy() {
    return Mockito.mock(AcceptingStrategy.class);
  }

}
