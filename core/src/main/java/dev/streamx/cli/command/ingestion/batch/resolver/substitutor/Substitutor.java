package dev.streamx.cli.command.ingestion.batch.resolver.substitutor;

import java.util.Map;

public interface Substitutor {

  Map<String, String> createSubstitutionVariables(String file, String channel, String relativePath);

  String substitute(Map<String, String> variables, String text);
}
