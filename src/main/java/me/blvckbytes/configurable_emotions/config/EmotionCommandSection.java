package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.lang.reflect.Field;
import java.util.List;

public class EmotionCommandSection extends ACommandSection {

  public static final String INITIAL_NAME = "emotion";

  public String allSentinel;
  public int paginationSize;

  public EmotionCommandSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(INITIAL_NAME, baseEnvironment);

    this.allSentinel = "all";
    this.paginationSize = 5;
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (this.paginationSize <= 0)
      throw new MappingError("Property \"paginationSize\" cannot be less than or equal to zero!");
  }
}
