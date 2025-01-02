package dev.streamx.cli.command.cloud.deploy;

import dev.streamx.cli.command.cloud.ProjectPathsService;
import dev.streamx.cli.command.cloud.deploy.Config.ConfigType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class ConfigService {

  @Inject
  DataService dataService;

  @Inject
  ProjectPathsService projectPathsService;

  @NotNull
  ConfigType getConfigType(Path dataSourcePath) {
    File dataSource = dataSourcePath.toFile();
    if (dataSource.isFile()) {
      return ConfigType.FILE;
    }
    if (dataSource.isDirectory()) {
      return ConfigType.DIR;
    }
    throw new IllegalStateException(
        "Config source " + dataSource + " provided in Mesh should be file or directory.");
  }

  @NotNull
  public Config getSecretVolume(Path projectPath, String configPath) {
    return getConfig(configPath, getSecretConfigPathMapper(projectPath),
        dataService::loadDataFromFiles, this::getConfigType);
  }

  @NotNull
  private Function<String, Path> getSecretConfigPathMapper(Path projectPath) {
    return (path) -> projectPathsService.resolveSecretPath(projectPath, path);
  }

  @NotNull
  public Config getConfigVolume(Path projectPath, String configPath) {
    return getConfig(configPath, (path) -> projectPathsService.resolveConfigPath(projectPath, path),
        dataService::loadDataFromFiles, this::getConfigType);
  }

  @NotNull
  public Config getSecretEnv(Path projectPath, String configPath) {
    return getConfig(configPath, getSecretConfigPathMapper(projectPath),
        dataService::loadDataFromProperties, (path -> ConfigType.FILE));
  }

  @NotNull
  public Config getConfigEnv(Path projectPath, String configPath) {
    return getConfig(configPath, (path) -> projectPathsService.resolveConfigPath(projectPath, path),
        dataService::loadDataFromProperties, (path) -> ConfigType.FILE);
  }

  @NotNull
  private Config getConfig(String configPath, Function<String, Path> pathMapper,
      Function<Path, Map<String, String>> dataMapper, Function<Path, ConfigType> configTypeMapper) {
    Path mappedPath = pathMapper.apply(configPath);
    Map<String, String> data = dataMapper.apply(mappedPath);
    ConfigType configType = configTypeMapper.apply(mappedPath);
    return new Config(configPath, data, configType);
  }
}