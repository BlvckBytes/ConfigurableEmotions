package me.blvckbytes.configurable_emotions.config.profle;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

@CSAlways
public class FlagsSection extends AConfigSection {

  public FlagSection titleEnabled;
  public FlagSection actionBarEnabled;
  public FlagSection chatEnabled;
  public FlagSection soundEnabled;
  public FlagSection effectEnabled;

  public FlagsSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
