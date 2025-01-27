package me.blvckbytes.configurable_emotions.config;

import com.cryptomorin.xseries.XSound;
import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EmotionSection extends AConfigSection {

  public boolean tryRegisterDirectly;
  public BukkitEvaluable description;

  public long cooldownSeconds;
  public boolean supportsSelf;
  public boolean supportsOthers;
  public boolean supportsAll;

  public @Nullable BukkitEvaluable soundSender;
  public @Nullable BukkitEvaluable soundReceiver;

  public List<DisplayedEffect> effectsSender;
  public List<DisplayedEffect> effectsReceiver;

  // Played the emotion on themselves

  public @Nullable DisplayedMessages messagesSelfSender;
  public @Nullable DisplayedMessages messagesSelfBroadcast;

  // Played the emotion at one other player

  public @Nullable DisplayedMessages messagesOneSender;
  public @Nullable DisplayedMessages messagesOneReceiver;
  public @Nullable DisplayedMessages messagesOneBroadcast;

  // Played the emotion at all other online players

  public @Nullable DisplayedMessages messagesAllSender;
  public @Nullable DisplayedMessages messagesAllReceiver;
  public @Nullable DisplayedMessages messagesAllBroadcast;

  @CSIgnore
  public @Nullable XSound _soundSender, _soundReceiver;

  public EmotionSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.effectsSender = new ArrayList<>();
    this.effectsReceiver = new ArrayList<>();
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
