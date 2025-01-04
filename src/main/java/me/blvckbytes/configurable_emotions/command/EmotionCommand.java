package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.config.EmotionCommandSection;
import me.blvckbytes.configurable_emotions.config.MainSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmotionCommand implements CommandExecutor, TabCompleter {

  private final ConfigKeeper<MainSection> config;

  public EmotionCommand(ConfigKeeper<MainSection> config) {
    this.config = config;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!CommandPermission.COMMAND_EMOTION.hasPermission(sender)) {
      config.rootSection.playerMessages.missingPermissionEmotionCommand.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return false;
    }

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!CommandPermission.COMMAND_EMOTION.hasPermission(sender))
      return List.of();

    return List.of();
  }
}
