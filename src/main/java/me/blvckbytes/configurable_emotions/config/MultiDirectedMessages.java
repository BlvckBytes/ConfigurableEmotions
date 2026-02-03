package me.blvckbytes.configurable_emotions.config;

import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import org.jetbrains.annotations.Nullable;

public class MultiDirectedMessages extends ConfigSection {

  public @Nullable DisplayedMessages toSender;
  public @Nullable DisplayedMessages toReceiver;
  public @Nullable DisplayedMessages asBroadcast;
  public @Nullable ComponentMarkup toDiscord;

  public MultiDirectedMessages(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);
  }
}
