package dev.streamx.cli.interpolation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.All;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ObjectMapperProducer {

  @Inject
  InterpolationSupport interpolationSupport;

  @Interpolating
  @ApplicationScoped
  ObjectMapper produce(@All List<ObjectMapperCustomizer> customizers) {
    ObjectMapper mapper = new ObjectMapper(new InterpolatingJsonFactory(interpolationSupport));

    // Apply all ObjectMapperCustomizer beans (incl. Quarkus)
    for (ObjectMapperCustomizer customizer : customizers) {
      customizer.customize(mapper);
    }
    return mapper;
  }
}
