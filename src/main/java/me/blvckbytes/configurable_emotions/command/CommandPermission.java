package me.blvckbytes.configurable_emotions.command;

import org.bukkit.permissions.Permissible;

public enum CommandPermission {
  COMMAND_EMOTION("emotion"),
  COMMAND_EMOTION_CONTROL("emotionctl")
  ;

  private static final String nodePrefix = "configurableemotions.command.";

  public final String node;

  CommandPermission(String nodeSuffix) {
    this.node = nodePrefix + nodeSuffix;
  }

  public static boolean hasEmotionPermission(Permissible permissible, String identifierLower) {
    return COMMAND_EMOTION.hasPermission(permissible) && permissible.hasPermission("configurableemotions.emotion." + identifierLower);
  }

  public static boolean hasCooldownBypassPermission(Permissible permissible, String identifierLower) {
    return permissible.hasPermission("configurableemotions.bypass-cooldown." + identifierLower);
  }

  public boolean hasPermission(Permissible permissible) {
    return permissible.hasPermission(node);
  }
}
