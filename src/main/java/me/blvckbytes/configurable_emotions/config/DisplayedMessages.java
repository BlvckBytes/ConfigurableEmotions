package me.blvckbytes.configurable_emotions.config;

import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import org.jetbrains.annotations.Nullable;

public class DisplayedMessages extends ConfigSection {

  public @Nullable ComponentMarkup chatMessage;
  public @Nullable ComponentMarkup actionBarMessage;
  public @Nullable ComponentMarkup titleMessage;
  public @Nullable ComponentMarkup subTitleMessage;

  public int titleFadeIn;
  public int titleStay;
  public int titleFadeOut;

  public DisplayedMessages(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);

    this.titleFadeIn = 5;
    this.titleStay = 35;
    this.titleFadeOut = 5;
  }
}
