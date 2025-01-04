package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.config.MainSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EmotionReloadCommand implements CommandExecutor {

  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  public EmotionReloadCommand(ConfigKeeper<MainSection> config, Logger logger) {
    this.config = config;
    this.logger = logger;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!CommandPermission.COMMAND_EMOTION_RELOAD.hasPermission(sender)) {
      config.rootSection.playerMessages.missingPermissionEmotionReloadCommand.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return false;
    }

    try {
      this.config.reload();

      config.rootSection.playerMessages.commandEmotionReloadSuccess.sendMessage(sender, config.rootSection.builtBaseEnvironment);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);

      config.rootSection.playerMessages.commandEmotionReloadFailure.sendMessage(sender, config.rootSection.builtBaseEnvironment);
    }

    return true;
  }
}
