package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class PlayerMessagesSection extends AConfigSection {

  public BukkitEvaluable actionReloadConfigSuccess;
  public BukkitEvaluable actionReloadConfigFailure;
  public BukkitEvaluable noUsageMessage;
  public BukkitEvaluable singleUsageMessage;
  public BukkitEvaluable multiUsageScreen;

  public BukkitEvaluable commandEmotionHelpScreen;
  public BukkitEvaluable playerProfileScreen;

  public BukkitEvaluable receiverCannotBeSelf;
  public BukkitEvaluable receivingPlayerNotOnline;
  public BukkitEvaluable receivingPlayerDuplicate;
  public BukkitEvaluable maximumNumberOfTargetsExceeded;
  public BukkitEvaluable cannotCombineAllSentinelWithNames;
  public BukkitEvaluable noReceivingPlayersOnline;
  public BukkitEvaluable unsupportedPlayingOnSelf;
  public BukkitEvaluable unsupportedOtherTarget;
  public BukkitEvaluable unsupportedAllTarget;
  public BukkitEvaluable awaitRemainingCooldown;
  public BukkitEvaluable noAccessToAnyEmotion;
  public BukkitEvaluable missingEmotionPermission;
  public BukkitEvaluable unknownEmotionProvided;
  public BukkitEvaluable playerOnlyCommand;
  public BukkitEvaluable missingPermissionEmotionCommand;
  public BukkitEvaluable missingPermissionEmotionControlCommand;

  public PlayerMessagesSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
