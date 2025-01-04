package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class EmotionCommandSection extends ACommandSection {

  public static final String INITIAL_NAME = "emotion";

  public String allSentinel;

  public EmotionCommandSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(INITIAL_NAME, baseEnvironment);

    this.allSentinel = "all";
  }
}
