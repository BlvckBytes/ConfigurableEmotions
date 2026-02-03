package me.blvckbytes.configurable_emotions.config;

import at.blvckbytes.cm_mapper.mapper.MappingError;
import at.blvckbytes.cm_mapper.section.command.CommandSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

import java.lang.reflect.Field;
import java.util.List;

public class EmotionCommandSection extends CommandSection {

  public static final String INITIAL_NAME = "emotion";

  public String allSentinel;
  public int paginationSize;

  public EmotionCommandSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(INITIAL_NAME, baseEnvironment, interpreterLogger);

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
