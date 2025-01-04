package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.config.EmotionSection;
import me.blvckbytes.configurable_emotions.config.MainSection;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EmotionCommand implements CommandExecutor, TabCompleter {

  private final ConfigKeeper<MainSection> config;
  private final Map<String, Map<UUID, Long>> lastExecutionByPlayerIdByIdentifierLower;

  public EmotionCommand(ConfigKeeper<MainSection> config) {
    this.config = config;
    this.lastExecutionByPlayerIdByIdentifierLower = new HashMap<>();
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage("§cThis command is only available to players!");
      return true;
    }

    if (!CommandPermission.COMMAND_EMOTION.hasPermission(player)) {
      config.rootSection.playerMessages.missingPermissionEmotionCommand.sendMessage(player, config.rootSection.builtBaseEnvironment);
      return false;
    }

    if (args.length == 1 || args.length == 2) {
      var identifierLower = args[0].toLowerCase();
      var emotion = config.rootSection.emotionByIdentifierLower.get(identifierLower);

      if (emotion == null) {
        player.sendMessage("§cUnknown emotion §4" + args[0] + " §cprovided!");
        return true;
      }

      if (!player.hasPermission("configurableemotions.emotion." + identifierLower)) {
        player.sendMessage("§cYou do not have permission to use the emotion §4" + args[0] + "§c!");
        return true;
      }

      if (emotion.cooldownSeconds != 0 && !player.hasPermission("configurableemotions.bypass-cooldown")) {
        var elapsedSeconds = getElapsedCooldownSeconds(identifierLower, player);
        var remainingSeconds = emotion.cooldownSeconds - elapsedSeconds;

        if (remainingSeconds > 0) {
          player.sendMessage("§cPlease wait another §4" + formatSecondsToTimeString(remainingSeconds) + " §cuntil playing this emotion again!");
          return true;
        }
      }

      if (args.length == 2) {
        var emotionTarget = args[1];

        if (emotionTarget.equalsIgnoreCase(config.rootSection.commands.emotion.allSentinel)) {
          if (!emotion.supportsAll) {
            player.sendMessage("§cThe emotion §4" + args[0] + " §cdoes not support being sent to all players!");
            return true;
          }

          if (!playEmotionAll(player, emotion)) {
            player.sendMessage("§cThere are no players online to receive your emotion!");
            return true;
          }

          if (emotion.cooldownSeconds != 0)
            touchLastExecutionStamp(identifierLower, player);

          return true;
        }

        if (!emotion.supportsOthers) {
          player.sendMessage("§cThe emotion §4" + args[0] + " §cdoes not support being sent to another player!");
          return true;
        }

        var targetPlayer = getPlayerByNameOrDisplayName(emotionTarget);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
          player.sendMessage("§cThe player §4" + args[1] + " §cis not currently online!");
          return true;
        }

        if (targetPlayer.equals(player)) {
          player.sendMessage("§cYou cannot send an emotion to yourself!");
          return true;
        }

        playEmotionOther(player, targetPlayer, emotion);

        if (emotion.cooldownSeconds != 0)
          touchLastExecutionStamp(identifierLower, player);

        return true;
      }

      if (!emotion.supportsSelf) {
        player.sendMessage("§cThe emotion §4" + args[0] + " §cdoes not support being played on yourself!");
        return true;
      }

      playEmotionSelf(player, emotion);

      if (emotion.cooldownSeconds != 0)
        touchLastExecutionStamp(identifierLower, player);

      return true;
    }

    var helpScreenEntries = new ArrayList<HelpScreenEntry>();

    for (var emotionEntry : config.rootSection.emotions.entrySet()) {
      var emotion = emotionEntry.getValue();

      helpScreenEntries.add(new HelpScreenEntry(
        emotionEntry.getKey(),
        emotion.description.asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment),
        emotion.supportsSelf,
        emotion.supportsOthers,
        emotion.supportsAll
      ));
    }

    config.rootSection.playerMessages.commandEmotionHelpScreen.sendMessage(
      player,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("label", label)
        .withStaticVariable("all_sentinel", config.rootSection.commands.emotion.allSentinel)
        .withStaticVariable("emotions", helpScreenEntries)
        .build()
    );

    return true;
  }

  private @Nullable Player getPlayerByNameOrDisplayName(String input) {
    for (var player : Bukkit.getOnlinePlayers()) {
      if (player.getName().equals(input) || player.getDisplayName().equals(input))
        return player;
    }

    return null;
  }

  private String formatSecondsToTimeString(long secondsTotal) {
    var hours  = secondsTotal / (60 * 60);
    var minutes = (secondsTotal - (hours * (60 * 60))) / 60;
    var seconds = secondsTotal - (hours * (60 * 60)) - (minutes * 60);

    var result = new StringBuilder();

    if (hours > 0)
      result.append(hours).append('h');

    if (minutes > 0) {
      if (!result.isEmpty())
        result.append(' ');

      result.append(minutes).append('m');
    }

    if (seconds > 0) {
      if (!result.isEmpty())
        result.append(' ');

      result.append(seconds).append('s');
    }

    return result.toString();
  }

  private boolean playEmotionAll(Player sender, EmotionSection emotion) {
    var receiverCount = 0;

    for (var receiver : Bukkit.getOnlinePlayers()) {
      if (receiver.equals(sender))
        continue;

      // TODO: Implement
      ++receiverCount;
    }

    var hadReceivers = receiverCount != 0;

    if (hadReceivers) {
      // TODO: Implement
    }

    return hadReceivers;
  }

  private void playEmotionOther(Player sender, Player receiver, EmotionSection emotion) {
    // TODO: Implement
  }

  private void playEmotionSelf(Player sender, EmotionSection emotion) {
    // TODO: Implement
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!CommandPermission.COMMAND_EMOTION.hasPermission(sender))
      return List.of();

    if (args.length == 1) {
      return config.rootSection.emotions.keySet()
        .stream()
        .filter(it -> StringUtils.containsIgnoreCase(it, args[0]))
        .toList();
    }

    var identifierLower = args[0].toLowerCase();
    var emotion = config.rootSection.emotionByIdentifierLower.get(identifierLower);

    if (emotion == null)
      return List.of();

    if (!sender.hasPermission("configurableemotions.emotion." + identifierLower))
      return List.of();

    if (!(emotion.supportsOthers || emotion.supportsAll))
      return List.of();

    var nameSuggestions = new ArrayList<String>();

    for (var player : Bukkit.getOnlinePlayers()) {
      if (player.equals(sender))
        continue;

      var name = player.getName();
      var displayName = player.getDisplayName();

      if (!name.equals(displayName)) {
        nameSuggestions.add(displayName);
        continue;
      }

      nameSuggestions.add(name);
    }

    if (emotion.supportsAll)
      nameSuggestions.add(config.rootSection.commands.emotion.allSentinel);

    return nameSuggestions;
  }

  private void touchLastExecutionStamp(String identifierLower, Player player) {
    lastExecutionByPlayerIdByIdentifierLower
      .computeIfAbsent(identifierLower, key -> new HashMap<>())
      .put(player.getUniqueId(), System.currentTimeMillis());
  }

  private int getElapsedCooldownSeconds(String identifierLower, Player player) {
    var lastExecutionByPlayer = lastExecutionByPlayerIdByIdentifierLower.get(identifierLower);

    if (lastExecutionByPlayer != null) {
      var lastExecutionStamp = lastExecutionByPlayer.get(player.getUniqueId());

      if (lastExecutionStamp != null)
        return (int) Math.round((System.currentTimeMillis() - lastExecutionStamp) / 1000.0);
    }

    return -1;
  }
}
