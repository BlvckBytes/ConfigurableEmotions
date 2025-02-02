package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class EmotionControlCommandSection extends ACommandSection {

  public static final String INITIAL_NAME = "emotionctl";

  public EmotionControlCommandSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(INITIAL_NAME, baseEnvironment);
  }
}
