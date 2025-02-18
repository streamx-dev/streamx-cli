package dev.streamx.cli.command.util;

import io.quarkus.arc.processor.Annotations;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.BuiltinScope;
import jakarta.inject.Singleton;
import java.util.List;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

public class CommandTransformer implements AnnotationsTransformer {

  private static final List<DotName> ANNOTATIONS = List.of(
      DotName.createSimple("picocli.CommandLine$Command")
  );

  public CommandTransformer() {
  }

  public boolean appliesTo(AnnotationTarget.Kind kind) {
    return Kind.CLASS == kind;
  }

  public void transform(AnnotationsTransformer.TransformationContext context) {
    if (!BuiltinScope.isIn(context.getAnnotations())) {
      if (Annotations.containsAny(context.getAnnotations(), ANNOTATIONS)) {
        context.transform().add(Singleton.class, new AnnotationValue[0]).done();
      }
    }
  }
}
