package me.blvckbytes.configurable_emotions.config;

import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

import java.lang.reflect.Field;
import java.util.List;

public class EssentialsDiscordSection extends ConfigSection {

  public boolean enabled;
  public String messageType;
  public boolean allowGroupMentions;

  public EssentialsDiscordSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }

  @Override
  public void afterParsing(List<Field> fields) {
    if (messageType == null || messageType.isBlank())
      throw new IllegalStateException("Property \"messageType\" cannot be null or blank!");

    if (!messageType.matches("^[a-z][a-z0-9-]*$"))
      throw new IllegalStateException("Property \"messageType\" must match the pattern \"^[a-z][a-z0-9-]*$\"!");
  }
}
