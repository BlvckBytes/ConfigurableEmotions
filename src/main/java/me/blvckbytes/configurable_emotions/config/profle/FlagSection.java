package me.blvckbytes.configurable_emotions.config.profle;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.configurable_emotions.profile.FlagValue;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class FlagSection extends AConfigSection {

  public FlagValue defaultValue = FlagValue.SELF_AND_OTHERS;

  public BukkitEvaluable toggleMessage;

  public FlagSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
