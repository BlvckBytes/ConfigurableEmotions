package me.blvckbytes.configurable_emotions.command;

import org.bukkit.permissions.Permissible;

public enum CommandPermission {
  COMMAND_EMOTION("emotion"),
  COMMAND_EMOTION_RELOAD("emotionrl")
  ;

  private static final String nodePrefix = "configurableemotions.command.";

  private final String node;

  CommandPermission(String nodeSuffix) {
    this.node = nodePrefix + nodeSuffix;
  }

  public static boolean hasEmotionPermission(Permissible permissible, String identifierLower) {
    return COMMAND_EMOTION.hasPermission(permissible) && permissible.hasPermission("configurableemotions.emotion." + identifierLower);
  }

  public boolean hasPermission(Permissible permissible) {
    return permissible.hasPermission(node);
  }
}
