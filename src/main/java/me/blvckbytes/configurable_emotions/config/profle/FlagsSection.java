package me.blvckbytes.configurable_emotions.config.profle;

import at.blvckbytes.cm_mapper.mapper.section.CSAlways;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

@CSAlways
public class FlagsSection extends ConfigSection {

  public FlagSection titleEnabled;
  public FlagSection actionBarEnabled;
  public FlagSection chatEnabled;
  public FlagSection soundEnabled;
  public FlagSection effectEnabled;

  public FlagsSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }
}
