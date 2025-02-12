package me.blvckbytes.configurable_emotions.config;

import com.cryptomorin.xseries.XSound;
import me.blvckbytes.bbconfigmapper.MappingError;
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

  public @Nullable BukkitEvaluable soundSender;
  public @Nullable BukkitEvaluable soundReceiver;

  public List<DisplayedEffect> effectsSender;
  public List<DisplayedEffect> effectsReceiver;

  private MultiDirectedMessages atSelfMessages;
  private @Nullable List<MultiDirectedMessages> additionalAtSelfMessages;

  private MultiDirectedMessages atOneMessages;
  private @Nullable List<MultiDirectedMessages> additionalAtOneMessages;

  private MultiDirectedMessages atManyMessages;
  private @Nullable List<MultiDirectedMessages> additionalAtManyMessages;

  private MultiDirectedMessages atAllMessages;
  private @Nullable List<MultiDirectedMessages> additionalAtAllMessages;

  @CSIgnore
  public @Nullable XSound _soundSender, _soundReceiver;

  public EmotionSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.effectsSender = new ArrayList<>();
    this.effectsReceiver = new ArrayList<>();
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

    if (soundSender != null) {
      if ((_soundSender = soundSender.asXSound(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"soundSender\" could not be corresponded to any valid sound");
    }

    if (soundReceiver != null) {
      if ((_soundReceiver = soundReceiver.asXSound(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"soundReceiver\" could not be corresponded to any valid sound");
    }
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
