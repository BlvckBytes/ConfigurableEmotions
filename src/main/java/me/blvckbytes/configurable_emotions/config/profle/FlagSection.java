package me.blvckbytes.configurable_emotions.config.profle;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class FlagSection extends AConfigSection {

  public boolean defaultValue = true;

  public BukkitEvaluable toggleOnMessage;
  public BukkitEvaluable toggleOffMessage;

  public FlagSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
