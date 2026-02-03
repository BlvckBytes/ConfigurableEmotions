package me.blvckbytes.configurable_emotions.config;

import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.cm_mapper.mapper.MappingError;
import at.blvckbytes.cm_mapper.mapper.section.CSIgnore;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Color;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayedEffect extends ConfigSection {

  private static final Map<String, Color> colorByConstantName;

  static {
    colorByConstantName = new HashMap<>();

    for (var field : Color.class.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers()))
        continue;

      if (field.getType() != Color.class)
        continue;

      try {
        colorByConstantName.put(field.getName(), (Color) field.get(null));
      } catch (Throwable ignored) {}
    }
  }

  public long frequencyTicks;
  public long numberOfExecutions;

  public @Nullable ComponentMarkup particle;
  public @Nullable ComponentMarkup particleMaterial;
  public @Nullable ComponentMarkup particleColor;
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

  public DisplayedEffect(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);

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
      var particleString = particle.asPlainString(null);
      var xParticle = XParticle.of(particleString);

      if (xParticle.isEmpty())
        throw new MappingError("Property \"particle\" with value \"" + particleString + "\" could not be corresponded to an XParticle");

      _particle = xParticle.get();
    }

    if (particleMaterial != null) {
      var materialString = particleMaterial.asPlainString(null);
      var xMaterial = XMaterial.matchXMaterial(materialString);

      if (xMaterial.isEmpty())
        throw new MappingError("Property \"particleMaterial\" with value \"" + materialString + "\" could not be corresponded to an XMaterial");
    }

    if (particleColor != null) {
      var colorString = particleColor.asPlainString(null);
      Color color = null;

      try {
        var parts = colorString.split(" ");
        color = Color.fromRGB(
          Integer.parseInt(parts[0]),
          Integer.parseInt(parts[1]),
          Integer.parseInt(parts[2])
        );
      } catch (Throwable ignored) {}

      if (color == null)
        color = colorByConstantName.get(colorString.toUpperCase().trim());

      if (color == null)
        throw new MappingError("Property \"particleColor\" with value \"" + colorString + "\" does not represent a valid bukkit- or RGB-color (\"R G B\")");

      _particleColor = color;
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

    if (helixHeight < 0) {
      throw new MappingError("Property \"helixHeight\" cannot be negative");
    }

    if (helixRadius < 0)
      throw new MappingError("Property \"helixRadius\" cannot be negative");

    if (helixWindings < 0)
      throw new MappingError("Property \"helixWindings\" cannot be negative");

    if (helixAngleStepSize < 0)
      throw new MappingError("Property \"helixAngleStepSize\" cannot be negative");
  }
}
