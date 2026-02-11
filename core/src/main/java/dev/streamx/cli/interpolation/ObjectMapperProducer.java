package dev.streamx.cli.interpolation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.quarkus.arc.All;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ObjectMapperProducer {

  @ApplicationScoped
  ObjectMapper produce(@All List<ObjectMapperCustomizer> customizers) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    // Apply all ObjectMapperCustomizer beans (incl. Quarkus)
    for (ObjectMapperCustomizer customizer : customizers) {
      customizer.customize(mapper);
    }
    return mapper;
  }
}
