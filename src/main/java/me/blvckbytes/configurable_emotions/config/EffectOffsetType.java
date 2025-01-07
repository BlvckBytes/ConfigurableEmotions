package me.blvckbytes.configurable_emotions.config;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public enum EffectOffsetType {
  FEET,
  EYES,
  ABOVE_HEAD
  ;

  public Location getForPlayer(Player player) {
    return switch (this) {
      case FEET -> player.getLocation();
      case EYES -> player.getEyeLocation();
      case ABOVE_HEAD -> player.getLocation().add(0, 2, 0);
    };
  }
}
