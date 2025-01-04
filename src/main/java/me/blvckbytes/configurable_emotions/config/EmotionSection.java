package me.blvckbytes.configurable_emotions.config;

import com.cryptomorin.xseries.XSound;
import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.bukkit.Effect;
import org.bukkit.Particle;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class EmotionSection extends AConfigSection {

  public BukkitEvaluable displayName;

  public long cooldownSeconds;

  public @Nullable BukkitEvaluable soundSender;
  public @Nullable BukkitEvaluable soundReceiver;

  public @Nullable DisplayedEffect effectSender;
  public @Nullable DisplayedEffect effectReceiver;

  public @Nullable BukkitEvaluable messageOneSender;
  public @Nullable BukkitEvaluable messageOneReceiver;
  public @Nullable BukkitEvaluable messageOneBroadcast;

  public @Nullable BukkitEvaluable messageAllSender;
  public @Nullable BukkitEvaluable messageAllReceiver;
  public @Nullable BukkitEvaluable messageAllBroadcast;

  @CSIgnore
  public @Nullable XSound _soundSender, _soundReceiver;

  public EmotionSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (displayName == null)
      throw new MappingError("Property \"displayName\" was absent, but is required");

    if (soundSender != null) {
      if ((_soundSender = soundSender.asXSound(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"soundSender\" could not be corresponded to any valid sound");
    }

    if (soundReceiver != null) {
      if ((_soundReceiver = soundReceiver.asXSound(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"soundReceiver\" could not be corresponded to any valid sound");
    }
  }
}
