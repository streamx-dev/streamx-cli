package dev.streamx.cli.v2.i18n;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

import java.lang.invoke.MethodHandles;

@MessageBundle(projectCode = "STREAMXCLI")
public interface MessageProvider {

  MessageProvider msg = Messages.getBundle(MethodHandles.lookup(), MessageProvider.class);

  @Message(id = 100, value = "No such settings property found: %s")
  String noSettingsPropertyFound(String key);

  @Message(id = 101, value = "Unable to get settings property")
  String unableToGetSettingsProperty();

  @Message(id = 102, value = "Failed to load properties from: %s")
  String failedToLoadPropertiesFrom(String path);

  @Message(id = 103, value = "Unable to set settings property")
  String unableToSetSettingsProperty();

  @Message(id = 104, value = "Unable to get settings file path")
  String unableToGetSettingsFilePath();

  @Message(id = 105, value = "Try '%s%s' for more information on the available options.%n")
  String tryForMoreInformationOnAvailableOptions(String qualifiedCommandName, String helpOptionName);

  @Message(id = 106, value = "Failed to read user input")
  String failedToReadUserInput();

  @Message(id = 107, value = "Failed to handle interactive input")
  String failedToHandleInteractiveInput();

  @Message(id = 108, value = "Unsupported output format")
  String unsupportedOutputFormat();
}