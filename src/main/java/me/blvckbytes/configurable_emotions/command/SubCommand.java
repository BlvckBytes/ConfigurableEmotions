package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.configurable_emotions.command.emotion_control.ControlAction;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface SubCommand {

  @Nullable CommandFailure onCommand(CommandSender sender, String[] args, Queue<NormalizedConstant<ControlAction>> queue);

  List<String> onTabComplete(CommandSender sender, String[] args);

  List<String> getPartialUsages(@Nullable Queue<NormalizedConstant<ControlAction>> actions, CommandSender sender);

  NormalizedConstant<ControlAction> getCorrespondingAction();

}
