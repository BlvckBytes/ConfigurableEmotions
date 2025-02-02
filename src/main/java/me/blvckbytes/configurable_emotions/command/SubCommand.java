package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.syllables_matcher.EnumMatcher;
import me.blvckbytes.syllables_matcher.EnumPredicate;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class SubCommand {

  public abstract @Nullable CommandFailure onCommand(CommandSender sender, String[] args, Queue<NormalizedConstant<?>> queue);

  public abstract List<String> onTabComplete(CommandSender sender, String[] args);

  public abstract List<String> getPartialUsages(@Nullable Queue<NormalizedConstant<?>> actions, CommandSender sender);

  public abstract NormalizedConstant<?> getCorrespondingAction();

  public static <T extends Enum<?>> @Nullable CommandFailure tryRelayCommand(
    EnumMatcher<T> matcher,
    @Nullable EnumPredicate<T> predicate,
    Map<NormalizedConstant<?>, SubCommand> subCommandMap,
    CommandSender sender, String[] args, Queue<NormalizedConstant<?>> actions
  ) {
    var subCommand = tryFindSubCommand(matcher, predicate, subCommandMap, args);

    if (subCommand == null)
      return CommandFailure.UNREGISTERED;

    var subArgs = new String[args.length - 1];
    System.arraycopy(args, 1, subArgs, 0, subArgs.length);

    actions.add(subCommand.getCorrespondingAction());

    return subCommand.onCommand(sender, subArgs, actions);
  }

  public static <T extends Enum<?>> List<String> tryRelayTabComplete(
    EnumMatcher<T> matcher,
    @Nullable EnumPredicate<T> predicate,
    Map<NormalizedConstant<?>, SubCommand> subCommandMap,
    CommandSender sender, String[] args
  ) {
    if (args.length == 1)
      return matcher.createCompletions(args[0]);

    var subCommand = tryFindSubCommand(matcher, predicate, subCommandMap, args);

    if (subCommand == null)
      return List.of();

    var subArgs = new String[args.length - 1];
    System.arraycopy(args, 1, subArgs, 0, subArgs.length);

    return subCommand.onTabComplete(sender, subArgs);
  }

  public static <T extends Enum<?>> @Nullable SubCommand tryFindSubCommand(
    EnumMatcher<T> matcher,
    @Nullable EnumPredicate<T> predicate,
    Map<NormalizedConstant<?>, SubCommand> subCommandMap,
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
