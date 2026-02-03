package me.blvckbytes.configurable_emotions.config;

import at.blvckbytes.cm_mapper.cm.ComponentExpression;
import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.cm_mapper.mapper.MappingError;
import at.blvckbytes.cm_mapper.mapper.section.CSAlways;
import at.blvckbytes.cm_mapper.mapper.section.CSIgnore;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import com.cryptomorin.xseries.XSound;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@CSAlways
public class EmotionSection extends ConfigSection {

  public boolean tryRegisterDirectly;
  public List<String> directAliases;
  public ComponentMarkup description;
  public boolean doesNoTargetEqualsAll;

  public long cooldownSeconds;
  public boolean supportsSelf;
  public boolean supportsOthers;
  public boolean supportsAll;
  public int maximumNumberOfTargets;
  public boolean broadcastToConsole;

  private @Nullable ComponentMarkup sound;
  private @Nullable ComponentExpression soundPitch;
  private @Nullable ComponentExpression soundVolume;

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

  public EmotionSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);

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
      var soundString = sound.asPlainString(null);
      var xSound = XSound.of(soundString);

      if (xSound.isEmpty())
        throw new MappingError("Property \"sound\" of value \"" + soundString + "\" could not be corresponded to an XSound");

      _sound = xSound.get();
    }

    if (soundPitch != null)
      _soundPitch = (float) ComponentExpression.asDouble(soundPitch, null);

    if (soundVolume != null)
      _soundVolume = (float) ComponentExpression.asDouble(soundVolume, null);
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
