package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.syllables_matcher.EnumMatcher;
import me.blvckbytes.syllables_matcher.EnumPredicate;
import org.bukkit.permissions.Permissible;

public enum ControlAction {
  RELOAD_CONFIG
  ;

  public static final EnumMatcher<ControlAction> matcher = new EnumMatcher<>(values());

  private final String node;

  ControlAction() {
    this.node = CommandPermission.COMMAND_EMOTION_CONTROL.node + "." + name().toLowerCase();
  }

  public boolean hasPermission(Permissible permissible) {
    return permissible.hasPermission(node);
  }

  public static EnumPredicate<ControlAction> filterFor(Permissible permissible) {
    return value -> value.constant.hasPermission(permissible);
  }
}
