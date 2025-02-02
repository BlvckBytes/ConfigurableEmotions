package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.config.MainSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmotionControlCommand implements CommandExecutor, TabCompleter {

  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  public EmotionControlCommand(
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.config = config;
    this.logger = logger;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!CommandPermission.COMMAND_EMOTION_CONTROL.hasPermission(sender)) {
      config.rootSection.playerMessages.missingPermissionActionReloadConfig.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return true;
    }

    if (args.length == 0) {
      printHelpScreen(sender);
      return true;
    }

    var actionFilter = ControlAction.filterFor(sender);
    var normalizedAction = ControlAction.matcher.matchFirst(args[0], actionFilter);

    if (normalizedAction == null) {
      printHelpScreen(sender);
      return true;
    }

    switch (normalizedAction.constant) {
      case RELOAD_CONFIG -> {
        if (args.length != 1) {
          printHelpScreen(sender);
          return true;
        }

        try {
          this.config.reload();

          config.rootSection.playerMessages.actionReloadConfigSuccess.sendMessage(sender, config.rootSection.builtBaseEnvironment);
        } catch (Exception e) {
          logger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);

          config.rootSection.playerMessages.actionReloadConfigFailure.sendMessage(sender, config.rootSection.builtBaseEnvironment);
        }
        break;
      }
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    return List.of();
  }

  private void printHelpScreen(CommandSender sender) {
    // TODO: Show help-screen
    sender.sendMessage("§cWrong usage!");
  }
}
