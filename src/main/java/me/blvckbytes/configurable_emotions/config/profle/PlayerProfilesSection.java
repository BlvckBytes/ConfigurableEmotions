package me.blvckbytes.configurable_emotions.config.profle;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class PlayerProfilesSection extends AConfigSection {

  @CSAlways
  public FlagsSection flags;

  public PlayerProfilesSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
