package me.blvckbytes.configurable_emotions;

import me.blvckbytes.configurable_emotions.config.DisplayedEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class EffectPlayer {

  private final Plugin plugin;

  public EffectPlayer(Plugin plugin) {
    this.plugin = plugin;
  }

  public void playEffect(DisplayedEffect effect, List<Player> targets) {
    if (effect.numberOfExecutions == 0 || effect._particle == null)
      return;

    Bukkit.getScheduler().runTask(plugin, () -> playEffectInstance(effect, targets, 1));
  }

  private void playEffectInstance(DisplayedEffect effect, List<Player> targets, int executionCounter) {
    // TODO: Play effect on the targets

    for (var target : targets)
      target.sendMessage("§a<Playing effect " + effect._particle.name() + ">");

    if (executionCounter < effect.numberOfExecutions)
      Bukkit.getScheduler().runTask(plugin, () -> playEffectInstance(effect, targets, executionCounter + 1));
  }
}
