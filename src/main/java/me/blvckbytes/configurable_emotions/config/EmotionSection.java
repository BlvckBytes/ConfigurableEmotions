package me.blvckbytes.configurable_emotions.config;

import com.cryptomorin.xseries.XSound;
import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class EmotionSection extends AConfigSection {

  public BukkitEvaluable description;

  public long cooldownSeconds;
  public boolean supportsSelf;
  public boolean supportsOthers;
  public boolean supportsAll;

  public @Nullable BukkitEvaluable soundSender;
  public @Nullable BukkitEvaluable soundReceiver;

  public @Nullable DisplayedEffect effectSender;
  public @Nullable DisplayedEffect effectReceiver;

  // Played the emotion on themselves

  public @Nullable BukkitEvaluable chatMessageSelfSender;
  public @Nullable BukkitEvaluable actionBarMessageSelfSender;
  public @Nullable BukkitEvaluable titleMessageSelfSender;
  public @Nullable BukkitEvaluable subTitleMessageSelfSender;

  public @Nullable BukkitEvaluable chatMessageSelfBroadcast;
  public @Nullable BukkitEvaluable actionBarMessageSelfBroadcast;
  public @Nullable BukkitEvaluable titleMessageSelfBroadcast;
  public @Nullable BukkitEvaluable subTitleMessageSelfBroadcast;

  // Played the emotion at one other player

  public @Nullable BukkitEvaluable chatMessageOneSender;
  public @Nullable BukkitEvaluable actionBarMessageOneSender;
  public @Nullable BukkitEvaluable titleMessageOneSender;
  public @Nullable BukkitEvaluable subTitleMessageOneSender;

  public @Nullable BukkitEvaluable chatMessageOneReceiver;
  public @Nullable BukkitEvaluable actionBarMessageOneReceiver;
  public @Nullable BukkitEvaluable titleMessageOneReceiver;
  public @Nullable BukkitEvaluable subTitleMessageOneReceiver;

  public @Nullable BukkitEvaluable chatMessageOneBroadcast;
  public @Nullable BukkitEvaluable actionBarMessageOneBroadcast;
  public @Nullable BukkitEvaluable titleMessageOneBroadcast;
  public @Nullable BukkitEvaluable subTitleMessageOneBroadcast;

  // Played the emotion at all other online players

  public @Nullable BukkitEvaluable chatMessageAllSender;
  public @Nullable BukkitEvaluable actionBarMessageAllSender;
  public @Nullable BukkitEvaluable titleMessageAllSender;
  public @Nullable BukkitEvaluable subTitleMessageAllSender;

  public @Nullable BukkitEvaluable chatMessageAllReceiver;
  public @Nullable BukkitEvaluable actionBarMessageAllReceiver;
  public @Nullable BukkitEvaluable titleMessageAllReceiver;
  public @Nullable BukkitEvaluable subTitleMessageAllReceiver;

  public @Nullable BukkitEvaluable chatMessageAllBroadcast;
  public @Nullable BukkitEvaluable actionBarMessageAllBroadcast;
  public @Nullable BukkitEvaluable titleMessageAllBroadcast;
  public @Nullable BukkitEvaluable subTitleMessageAllBroadcast;

  @CSIgnore
  public @Nullable XSound _soundSender, _soundReceiver;

  public EmotionSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (!(supportsSelf || supportsOthers || supportsAll))
      throw new MappingError("At least one of the properties \"supportsSelf\", \"supportsOthers\" or \"supportsAll\" must be enabled!");

    if (description == null)
      throw new MappingError("Property \"description\" was absent, but is required");

    if (soundSender != null) {
      if ((_soundSender = soundSender.asXSound(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"soundSender\" could not be corresponded to any valid sound");
    }

    if (soundReceiver != null) {
      if ((_soundReceiver = soundReceiver.asXSound(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"soundReceiver\" could not be corresponded to any valid sound");
    }
  }
}
