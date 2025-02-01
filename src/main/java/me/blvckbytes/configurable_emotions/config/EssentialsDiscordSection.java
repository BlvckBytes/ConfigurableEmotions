package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.lang.reflect.Field;
import java.util.List;

public class EssentialsDiscordSection extends AConfigSection {

  public boolean enabled;
  public String messageType;
  public boolean allowGroupMentions;

  public EssentialsDiscordSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    if (messageType == null || messageType.isBlank())
      throw new IllegalStateException("Property \"messageType\" cannot be null or blank!");

    if (!messageType.matches("^[a-z][a-z0-9-]*$"))
      throw new IllegalStateException("Property \"messageType\" must match the pattern \"^[a-z][a-z0-9-]*$\"!");
  }
}
