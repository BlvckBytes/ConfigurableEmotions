package me.blvckbytes.configurable_emotions.command.emotion_control;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.command.CommandFailure;
import me.blvckbytes.configurable_emotions.command.CommandPermission;
import me.blvckbytes.configurable_emotions.command.SubCommand;
import me.blvckbytes.configurable_emotions.command.emotion_control.sub.*;
import me.blvckbytes.configurable_emotions.config.MainSection;
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

  private final Map<NormalizedConstant<?>, SubCommand> subCommands;

  public EmotionControlCommand(
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.config = config;

    this.subCommands = new LinkedHashMap<>();

    registerSubCommand(new ReloadConfigCommand(config, logger));
    registerSubCommand(new ToggleActionBarCommand());
    registerSubCommand(new ToggleChatCommand());
    registerSubCommand(new ToggleEffectCommand());
    registerSubCommand(new ToggleSoundCommand());
    registerSubCommand(new ToggleTitleCommand());
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!CommandPermission.COMMAND_EMOTION_CONTROL.hasPermission(sender)) {
      config.rootSection.playerMessages.missingPermissionEmotionControlCommand.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return true;
    }

    var actions = new ArrayDeque<NormalizedConstant<?>>();
    var result = SubCommand.tryRelayCommand(ControlAction.matcher, ControlAction.filterFor(sender), subCommands, sender, args, actions);

    if (result == CommandFailure.INVALID_USAGE || result == CommandFailure.UNREGISTERED) {
      printHelp(actions, sender, label);
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

    return SubCommand.tryRelayTabComplete(ControlAction.matcher, ControlAction.filterFor(sender), subCommands, sender, args);
  }

  private void registerSubCommand(SubCommand command) {
    this.subCommands.put(command.getCorrespondingAction(), command);
  }

  private List<String> decidePartialUsages(@Nullable Queue<NormalizedConstant<?>> actions, CommandSender sender) {
    var partialUsages = new ArrayList<String>();

    NormalizedConstant<?> action;
    SubCommand actionTarget;

    if (actions == null || (action = actions.poll()) == null || (actionTarget = subCommands.get(action)) == null) {
      for (var subCommand : subCommands.values())
        partialUsages.addAll(subCommand.getPartialUsages(null, sender));

      return partialUsages;
    }

    return actionTarget.getPartialUsages(actions, sender);
  }

  private void printHelp(@Nullable Queue<NormalizedConstant<?>> actions, CommandSender sender, String label) {
    var usages = decidePartialUsages(actions, sender)
      .stream()
      .map(it -> "/" + label + " " + it)
      .toList();

    if (usages.size() == 1) {
      config.rootSection.playerMessages.singleUsageMessage.sendMessage(
        sender,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("usage", usages.get(0))
          .build()
      );
      return;
    }

    if (usages.isEmpty()) {
      config.rootSection.playerMessages.noUsageMessage.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return;
    }

    config.rootSection.playerMessages.multiUsageScreen.sendMessage(
      sender,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("usages", usages)
        .build()
    );
  }
}
