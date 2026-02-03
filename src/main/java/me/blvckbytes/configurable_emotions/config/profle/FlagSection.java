package me.blvckbytes.configurable_emotions.config.profle;

import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import me.blvckbytes.configurable_emotions.profile.FlagValue;

public class FlagSection extends ConfigSection {

  public FlagValue defaultValue = FlagValue.SELF_AND_OTHERS;

  public ComponentMarkup toggleMessage;

  public FlagSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }
}
