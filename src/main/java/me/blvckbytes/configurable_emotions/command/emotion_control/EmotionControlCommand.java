package me.blvckbytes.configurable_emotions.command.emotion_control;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.command.CommandFailure;
import me.blvckbytes.configurable_emotions.command.CommandPermission;
import me.blvckbytes.configurable_emotions.command.SubCommand;
import me.blvckbytes.configurable_emotions.command.emotion_control.sub.*;
import me.blvckbytes.configurable_emotions.config.MainSection;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileStore;
import me.blvckbytes.syllables_matcher.EnumMatcher;
import me.blvckbytes.syllables_matcher.EnumPredicate;
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

  // TODO: This command-code is a horrible mess, copied over quickly from another project... Clean up!

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

    var actions = new ArrayDeque<NormalizedConstant<ControlAction>>();
    var result = tryRelayCommand(ControlAction.matcher, ControlAction.filterFor(sender), subCommands, sender, args, actions);

    if (result == CommandFailure.UNREGISTERED) {
      if (args.length == 0) {
        printHelp(actions, sender, label);
        return true;
      }

      config.rootSection.playerMessages.noUsageMessage.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return true;
    }

    if (result == CommandFailure.INVALID_USAGE) {
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

    return tryRelayTabComplete(ControlAction.matcher, ControlAction.filterFor(sender), subCommands, sender, args);
  }

  private void registerSubCommand(SubCommand command) {
    this.subCommands.put(command.getCorrespondingAction(), command);
  }

  private List<String> decidePartialUsages(@Nullable Queue<NormalizedConstant<ControlAction>> actions, CommandSender sender) {
    var partialUsages = new ArrayList<String>();

    NormalizedConstant<ControlAction> action;
    SubCommand actionTarget;

    if (actions == null || (action = actions.poll()) == null || (actionTarget = subCommands.get(action)) == null) {
      var predicate = ControlAction.filterFor(sender);

      for (var subCommandEntry : subCommands.entrySet()) {
        if (!predicate.test(subCommandEntry.getKey()))
          continue;

        partialUsages.addAll(subCommandEntry.getValue().getPartialUsages(null, sender));
      }

      return partialUsages;
    }

    return actionTarget.getPartialUsages(actions, sender);
  }

  private void printHelp(@Nullable Queue<NormalizedConstant<ControlAction>> actions, CommandSender sender, String label) {
    var usages = decidePartialUsages(actions , sender)
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

  private static @Nullable CommandFailure tryRelayCommand(
    EnumMatcher<ControlAction> matcher,
    @Nullable EnumPredicate<ControlAction> predicate,
    Map<NormalizedConstant<ControlAction>, SubCommand> subCommandMap,
    CommandSender sender, String[] args, Queue<NormalizedConstant<ControlAction>> actions
  ) {
    var subCommand = tryFindSubCommand(matcher, predicate, subCommandMap, args);

    if (subCommand == null)
      return CommandFailure.UNREGISTERED;

    var subArgs = new String[args.length - 1];
    System.arraycopy(args, 1, subArgs, 0, subArgs.length);

    actions.add(subCommand.getCorrespondingAction());

    return subCommand.onCommand(sender, subArgs, actions);
  }

  private static List<String> tryRelayTabComplete(
    EnumMatcher<ControlAction> matcher,
    @Nullable EnumPredicate<ControlAction> predicate,
    Map<NormalizedConstant<ControlAction>, SubCommand> subCommandMap,
    CommandSender sender, String[] args
  ) {
    if (args.length == 1)
      return matcher.createCompletions(args[0], predicate);

    var subCommand = tryFindSubCommand(matcher, predicate, subCommandMap, args);

    if (subCommand == null)
      return List.of();

    var subArgs = new String[args.length - 1];
    System.arraycopy(args, 1, subArgs, 0, subArgs.length);

    return subCommand.onTabComplete(sender, subArgs);
  }

  private static @Nullable SubCommand tryFindSubCommand(
    EnumMatcher<ControlAction> matcher,
    @Nullable EnumPredicate<ControlAction> predicate,
    Map<NormalizedConstant<ControlAction>, SubCommand> subCommandMap,
    String[] args
  ) {
    if (args.length == 0)
      return null;

    var matchedAction = matcher.matchFirst(args[0], predicate);

    if (matchedAction == null)
      return null;

    return subCommandMap.get(matchedAction);
  }
}
