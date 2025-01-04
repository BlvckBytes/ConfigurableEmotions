package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
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

    if (args.length == 0) {
      var helpScreenEntries = config.rootSection.configuredEmotions.stream().map(configuredEmotion -> new HelpScreenEntry(
        configuredEmotion.identifier(),
        configuredEmotion.emotion().description.asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment),
        configuredEmotion.emotion().supportsSelf,
        configuredEmotion.emotion().supportsOthers,
        configuredEmotion.emotion().supportsAll
      )).toList();

      config.rootSection.playerMessages.commandEmotionHelpScreen.sendMessage(
        sender,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("label", label)
          .withStaticVariable("all_sentinel", config.rootSection.commands.emotion.allSentinel)
          .withStaticVariable("emotions", helpScreenEntries)
          .build()
      );

      return true;
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
