package me.blvckbytes.configurable_emotions;

import me.blvckbytes.configurable_emotions.config.DisplayedEffect;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileFlag;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class EffectPlayer {

  private static final Random LOCAL_RANDOM = ThreadLocalRandom.current();

  private final PlayerProfileStore profileStore;
  private final Plugin plugin;
  private final Logger logger;

  public EffectPlayer(
    PlayerProfileStore profileStore,
    Plugin plugin,
    Logger logger
  ) {
    this.profileStore = profileStore;
    this.plugin = plugin;
    this.logger = logger;
  }

  public void playEffect(DisplayedEffect effect, boolean isBroadcast, Collection<Player> targets) {
    if (effect.numberOfExecutions == 0 || effect._particle == null)
      return;

    playEffectInstance(effect, isBroadcast, targets, 1);
  }

  private void playEffectInstance(DisplayedEffect effect, boolean isBroadcast, Collection<Player> targets, int executionCounter) {
    for (var target : targets) {
      // As of now, effects only play for sender/receiver, so they're always the target of the emotion
      if (!profileStore.getProfile(target).getFlagOrDefault(PlayerProfileFlag.PARTICLE_EFFECT_ENABLED).doesShow(!isBroadcast))
        continue;

      var effectLocation = target.getLocation().add(0, effect.yOffset, 0);

      switch (effect.displayType) {
        case SINGLE -> playParticle(effect, target, effectLocation);
        case CLOUD -> playEffectCloud(effect, target, effectLocation);
        case HELIX -> playEffectHelix(effect, target, effectLocation);
      }
    }

    if (executionCounter < effect.numberOfExecutions) {
      Bukkit.getScheduler().runTaskLater(
        plugin,
        () -> playEffectInstance(effect, isBroadcast, targets, executionCounter + 1),
        effect.frequencyTicks
      );
    }
  }

  private void playEffectHelix(DisplayedEffect effect, Player target, Location location) {
    var phaseShiftUnit = (2 * Math.PI) / effect.numberOfHelixCurves;

    int numberOfAngleSteps = (int) ((2 * Math.PI) / effect.helixAngleStepSize);
    double heightStepUnit = effect.helixHeight / numberOfAngleSteps / effect.helixWindings;

    double alpha = 0;
    double deltaY = 0;

    for (var alphaStepIndex = 0; alphaStepIndex < numberOfAngleSteps * effect.helixWindings; ++alphaStepIndex) {
      for (var curveIndex = 0; curveIndex < effect.numberOfHelixCurves; ++curveIndex) {
        var phaseShift = phaseShiftUnit * curveIndex;

        var deltaZ = effect.helixRadius * Math.sin(alpha + phaseShift);
        var deltaX = effect.helixRadius * Math.cos(alpha + phaseShift);

        playParticle(effect, target, location.clone().add(deltaX, deltaY, deltaZ));
      }

      deltaY += heightStepUnit;
      alpha += effect.helixAngleStepSize;
    }
  }

  private double generateRandomRadiusOffset(double radius) {
    var radiusScaleFactor = LOCAL_RANDOM.nextDouble();

    if (LOCAL_RANDOM.nextBoolean())
      return -radius * radiusScaleFactor;

    return radius * radiusScaleFactor;
  }

  private void playEffectCloud(DisplayedEffect effect, Player target, Location location) {
    for (var i = 0; i < effect.cloudParticleCount; ++i) {
      var particleLocation = location.clone().add(
        generateRandomRadiusOffset(effect.cloudRadius),
        generateRandomRadiusOffset(effect.cloudRadius),
        generateRandomRadiusOffset(effect.cloudRadius)
      );

      playParticle(effect, target, particleLocation);
    }
  }

  private void playParticle(DisplayedEffect effect, Player target, Location location) {
    var particle = Objects.requireNonNull(effect._particle).get();

    Object parameter = null;

    if (particle.getDataType() == Particle.DustOptions.class && effect._particleColor != null)
      parameter = new Particle.DustOptions(effect._particleColor, effect.particleSize);

    if (particle.getDataType() == BlockData.class && effect._particleMaterial != null) {
      var parsedMaterial = effect._particleMaterial.parseMaterial();

      if (parsedMaterial != null)
        parameter = parsedMaterial.createBlockData();

      else
        logger.severe("Could not parse particle-material " + effect._particleMaterial.name());
    }

    target.spawnParticle(particle, location, 1, parameter);
  }
}
