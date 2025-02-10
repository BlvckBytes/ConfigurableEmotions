package me.blvckbytes.configurable_emotions.command.emotion_control;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.command.CommandFailure;
import me.blvckbytes.configurable_emotions.command.CommandPermission;
import me.blvckbytes.configurable_emotions.command.SubCommand;
import me.blvckbytes.configurable_emotions.command.emotion_control.sub.*;
import me.blvckbytes.configurable_emotions.config.MainSection;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileStore;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

public class EmotionControlCommand implements CommandExecutor, TabCompleter {

  private final ConfigKeeper<MainSection> config;
  private final Map<NormalizedConstant<ControlAction>, SubCommand> subCommands;

  public EmotionControlCommand(
    PlayerProfileStore profileStore,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.config = config;

    this.subCommands = new LinkedHashMap<>();

    registerSubCommand(new ToggleTitleCommand(config, profileStore));
    registerSubCommand(new ToggleActionBarCommand(config, profileStore));
    registerSubCommand(new ToggleChatCommand(config, profileStore));
    registerSubCommand(new ToggleSoundCommand(config, profileStore));
    registerSubCommand(new ToggleParticleEffectCommand(config, profileStore));
    registerSubCommand(new ProfileCommand(config, profileStore));
    registerSubCommand(new ReloadConfigCommand(config, logger));
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!CommandPermission.COMMAND_EMOTION_CONTROL.hasPermission(sender)) {
      config.rootSection.playerMessages.missingPermissionEmotionControlCommand.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return true;
    }

    var subCommand = tryFindSubCommand(sender, args);

    if (subCommand == null) {
      if (args.length == 0) {
        config.rootSection.playerMessages.multiUsageScreen.sendMessage(
          sender,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable(
              "usages",
              subCommands.values()
                .stream()
                .map(it -> "/" + label + " " + it.getPartialUsage(sender))
                .toList()
            )
            .build()
        );
        return true;
      }

      config.rootSection.playerMessages.noUsageMessage.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return true;
    }

    var subArgs = new String[args.length - 1];
    System.arraycopy(args, 1, subArgs, 0, subArgs.length);

    var result = subCommand.onCommand(sender, subArgs);

    if (result == CommandFailure.INVALID_USAGE) {
      config.rootSection.playerMessages.singleUsageMessage.sendMessage(
        sender,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("usage", subCommand.getPartialUsage(sender))
          .build()
      );
      return true;
    }

    if (result == CommandFailure.PLAYER_ONLY) {
      config.rootSection.playerMessages.playerOnlyCommand.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return true;
    }

    return true;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!CommandPermission.COMMAND_EMOTION_CONTROL.hasPermission(sender))
      return List.of();

    return tryRelayTabComplete(sender, args);
  }

  private void registerSubCommand(SubCommand command) {
    this.subCommands.put(command.getCorrespondingAction(), command);
  }

  private List<String> tryRelayTabComplete(CommandSender sender, String[] args) {
    if (args.length == 1)
      return ControlAction.matcher.createCompletions(args[0], ControlAction.filterFor(sender));

    var subCommand = tryFindSubCommand(sender, args);

    if (subCommand == null)
      return List.of();

    var subArgs = new String[args.length - 1];
    System.arraycopy(args, 1, subArgs, 0, subArgs.length);

    return subCommand.onTabComplete(sender, subArgs);
  }

  private @Nullable SubCommand tryFindSubCommand(CommandSender sender, String[] args) {
    if (args.length == 0)
      return null;

    var matchedAction = ControlAction.matcher.matchFirst(args[0], ControlAction.filterFor(sender));

    if (matchedAction == null)
      return null;

    return subCommands.get(matchedAction);
  }
}
