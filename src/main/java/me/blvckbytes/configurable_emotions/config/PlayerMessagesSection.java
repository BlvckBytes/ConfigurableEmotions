package me.blvckbytes.configurable_emotions.config;

import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

public class PlayerMessagesSection extends ConfigSection {

  public ComponentMarkup profileFlagToggled;
  public ComponentMarkup actionReloadConfigSuccess;
  public ComponentMarkup actionReloadConfigFailure;
  public ComponentMarkup noUsageMessage;
  public ComponentMarkup singleUsageMessage;
  public ComponentMarkup multiUsageScreen;
  public ComponentMarkup commandEmotionHelpScreen;
  public ComponentMarkup playerProfileScreen;
  public ComponentMarkup receiverCannotBeSelf;
  public ComponentMarkup receivingPlayerNotOnline;
  public ComponentMarkup receivingPlayerDuplicate;
  public ComponentMarkup maximumNumberOfTargetsExceeded;
  public ComponentMarkup cannotCombineAllSentinelWithNames;
  public ComponentMarkup noReceivingPlayersOnline;
  public ComponentMarkup unsupportedPlayingOnSelf;
  public ComponentMarkup unsupportedOtherTarget;
  public ComponentMarkup unsupportedAllTarget;
  public ComponentMarkup awaitRemainingCooldown;
  public ComponentMarkup noAccessToAnyEmotion;
  public ComponentMarkup missingEmotionPermission;
  public ComponentMarkup unknownEmotionProvided;
  public ComponentMarkup playerOnlyCommand;
  public ComponentMarkup missingPermissionEmotionCommand;
  public ComponentMarkup missingPermissionEmotionControlCommand;

  public PlayerMessagesSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }
}
