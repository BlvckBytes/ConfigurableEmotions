package me.blvckbytes.configurable_emotions.config;

import com.cryptomorin.xseries.XSound;
import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@CSAlways
public class EmotionSection extends AConfigSection {

  public boolean tryRegisterDirectly;
  public List<String> directAliases;
  public BukkitEvaluable description;
  public boolean doesNoTargetEqualsAll;

  public long cooldownSeconds;
  public boolean supportsSelf;
  public boolean supportsOthers;
  public boolean supportsAll;
  public int maximumNumberOfTargets;
  public boolean broadcastToConsole;

  private @Nullable BukkitEvaluable sound;
  private @Nullable BukkitEvaluable soundPitch;
  private @Nullable BukkitEvaluable soundVolume;

  @CSIgnore
  public @Nullable XSound _sound;

  @CSIgnore
  public float _soundPitch = 1;

  @CSIgnore
  public float _soundVolume = 1;

  public List<DisplayedEffect> effects;

  private MultiDirectedMessages atSelfMessages;
  private @Nullable List<MultiDirectedMessages> additionalAtSelfMessages;

  private MultiDirectedMessages atOneMessages;
  private @Nullable List<MultiDirectedMessages> additionalAtOneMessages;

  private MultiDirectedMessages atManyMessages;
  private @Nullable List<MultiDirectedMessages> additionalAtManyMessages;

  private MultiDirectedMessages atAllMessages;
  private @Nullable List<MultiDirectedMessages> additionalAtAllMessages;

  public EmotionSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.effects = new ArrayList<>();
    this.directAliases = new ArrayList<>();
    this.maximumNumberOfTargets = 1;
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (!(supportsSelf || supportsOthers || supportsAll))
      throw new MappingError("At least one of the properties \"supportsSelf\", \"supportsOthers\" or \"supportsAll\" must be enabled!");

    if (supportsOthers) {
      if (maximumNumberOfTargets <= 0)
        throw new MappingError("Property \"maximumNumberOfTargets\" cannot be less than or equal to zero");
    }

    if (description == null)
      throw new MappingError("Property \"description\" was absent, but is required");

    if (sound != null) {
      if ((_sound = sound.asXSound(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"sound\" could not be corresponded to any valid sound");
    }

    if (soundPitch != null)
      _soundPitch = soundPitch.asScalar(ScalarType.DOUBLE, builtBaseEnvironment).floatValue();

    if (soundVolume != null)
      _soundVolume = soundVolume.asScalar(ScalarType.DOUBLE, builtBaseEnvironment).floatValue();
  }

  public MultiDirectedMessages accessAtSelfMessages() {
    return chooseRandomizedMessages(atSelfMessages, additionalAtSelfMessages);
  }

  public MultiDirectedMessages accessAtOneMessages() {
    return chooseRandomizedMessages(atOneMessages, additionalAtOneMessages);
  }

  public MultiDirectedMessages accessAtManyMessages() {
    return chooseRandomizedMessages(atManyMessages, additionalAtManyMessages);
  }

  public MultiDirectedMessages accessAtAllMessages() {
    return chooseRandomizedMessages(atAllMessages, additionalAtAllMessages);
  }

  private static MultiDirectedMessages chooseRandomizedMessages(MultiDirectedMessages main, @Nullable List<MultiDirectedMessages> additional) {
    if (additional == null)
      return main;

    var additionalCount = additional.size();
    var itemIndex = ThreadLocalRandom.current().nextInt(additionalCount + 1);

    if (itemIndex == additionalCount)
      return main;

    return additional.get(itemIndex);
  }
}
