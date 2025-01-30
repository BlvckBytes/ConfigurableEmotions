package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

public class DisplayedMessages extends AConfigSection {

  public @Nullable BukkitEvaluable chatMessage;
  public @Nullable BukkitEvaluable actionBarMessage;
  public @Nullable BukkitEvaluable titleMessage;
  public @Nullable BukkitEvaluable subTitleMessage;

  public int titleFadeIn;
  public int titleStay;
  public int titleFadeOut;

  public DisplayedMessages(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.titleFadeIn = 5;
    this.titleStay = 35;
    this.titleFadeOut = 5;
  }
}
