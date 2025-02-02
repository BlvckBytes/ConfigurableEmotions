package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.configurable_emotions.command.emotion_control.ControlAction;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class SubCommand {

  public abstract @Nullable CommandFailure onCommand(CommandSender sender, String[] args, Queue<NormalizedConstant<ControlAction>> queue);

  public abstract List<String> onTabComplete(CommandSender sender, String[] args);

  public abstract List<String> getPartialUsages(@Nullable Queue<NormalizedConstant<ControlAction>> actions, CommandSender sender);

  public abstract NormalizedConstant<ControlAction> getCorrespondingAction();

}
