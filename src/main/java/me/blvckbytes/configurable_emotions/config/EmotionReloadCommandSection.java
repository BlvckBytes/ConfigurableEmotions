package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class EmotionReloadCommandSection extends ACommandSection {

  public static final String INITIAL_NAME = "emotionrl";

  public EmotionReloadCommandSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(INITIAL_NAME, baseEnvironment);
  }
}
