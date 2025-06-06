package me.blvckbytes.configurable_emotions.config;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;
import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import org.bukkit.Color;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class DisplayedEffect extends AConfigSection {

  public long frequencyTicks;
  public long numberOfExecutions;

  public @Nullable BukkitEvaluable particle;
  public @Nullable BukkitEvaluable particleMaterial;
  public @Nullable BukkitEvaluable particleColor;
  public float particleSize;

  public EffectDisplayType displayType;
  public double yOffset;

  public double cloudRadius;
  public int cloudParticleCount;

  public int numberOfHelixCurves;
  public double helixHeight;
  public double helixRadius;
  public int helixWindings;
  public double helixAngleStepSize;

  @CSIgnore
  public @Nullable XParticle _particle;

  @CSIgnore
  public @Nullable XMaterial _particleMaterial;

  @CSIgnore
  public @Nullable Color _particleColor;

  public DisplayedEffect(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.displayType = EffectDisplayType.SINGLE;
    this.yOffset = 0;
    this.particleSize = 1;

    this.frequencyTicks = 5;
    this.numberOfExecutions = 1;

    this.cloudRadius = 4;
    this.cloudParticleCount = 20;

    this.numberOfHelixCurves = 2;
    this.helixHeight = 2;
    this.helixRadius = 1;
    this.helixAngleStepSize = .1;
    this.helixWindings = 1;
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (particle != null) {
      if ((_particle = particle.asXParticle(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"particle\" could not be corresponded to any valid particle");
    }

    if (particleMaterial != null) {
      if ((_particleMaterial = particleMaterial.asXMaterial(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"particleMaterial\" could not be corresponded to any valid material");
    }

    if (particleColor != null) {
      if ((_particleColor = particleColor.asBukkitColor(builtBaseEnvironment)) == null)
        throw new MappingError("Property \"particleColor\" does not represent a valid bukkit- or RGB-color (\"R G B\")");
    }

    if (yOffset < 0)
      throw new MappingError("Property \"yOffset\" cannot be negative");

    if (frequencyTicks < 0)
      throw new MappingError("Property \"frequencyTicks\" cannot be negative");

    if (numberOfExecutions < 0)
      throw new MappingError("Property \"numberOfExecutions\" cannot be negative");

    if (cloudRadius < 0)
      throw new MappingError("Property \"cloudRadius\" cannot be negative");

    if (cloudParticleCount < 0)
      throw new MappingError("Property \"cloudParticleCount\" cannot be negative");

    if (numberOfHelixCurves < 0)
      throw new MappingError("Property \"numberOfHelixCurves\" cannot be negative");

    if (helixHeight < 0)
      throw new MappingError("Property \"helixHeight\" cannot be negative");

    if (helixRadius < 0)
      throw new MappingError("Property \"helixRadius\" cannot be negative");

    if (helixWindings < 0)
      throw new MappingError("Property \"helixWindings\" cannot be negative");

    if (helixAngleStepSize < 0)
      throw new MappingError("Property \"helixAngleStepSize\" cannot be negative");
  }
}
