package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.lang.reflect.Field;
import java.util.List;

public class EmoteSection extends AConfigSection {

  public BukkitEvaluable displayName;
  public long cooldownSeconds;

  public EmoteSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (displayName == null)
      throw new MappingError("Property \"displayName\" was absent, but is required");
  }
}
