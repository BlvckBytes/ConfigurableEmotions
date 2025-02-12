package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

public class MultiDirectedMessages extends AConfigSection {

  public @Nullable DisplayedMessages toSender;
  public @Nullable DisplayedMessages toReceiver;
  public @Nullable DisplayedMessages asBroadcast;
  public @Nullable BukkitEvaluable toDiscord;

  public MultiDirectedMessages(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
