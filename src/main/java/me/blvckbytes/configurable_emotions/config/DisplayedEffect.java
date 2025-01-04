package me.blvckbytes.configurable_emotions.config;

import com.cryptomorin.xseries.particles.XParticle;
import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class DisplayedEffect extends AConfigSection {

  public @Nullable BukkitEvaluable particle;
  public EffectDisplayType displayType;
  public EffectOffsetType offsetType;

  @CSIgnore
  public @Nullable XParticle _particle;

  public DisplayedEffect(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.displayType = EffectDisplayType.SINGLE;
    this.offsetType = EffectOffsetType.EYES;
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (particle != null) {
      if ((_particle = particle.asXParticle(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"particle\" could not be corresponded to any valid particle");
    }
  }
}
