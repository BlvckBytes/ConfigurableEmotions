package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.EffectPlayer;
import me.blvckbytes.configurable_emotions.config.DisplayedMessages;
import me.blvckbytes.configurable_emotions.config.EmotionSection;
import me.blvckbytes.configurable_emotions.config.MainSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
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

  private final EffectPlayer effectPlayer;
  private final ConfigKeeper<MainSection> config;
  private final Map<String, Map<UUID, Long>> lastExecutionByPlayerIdByIdentifierLower;

  public EmotionCommand(
    EffectPlayer effectPlayer,
    ConfigKeeper<MainSection> config
  ) {
    this.effectPlayer = effectPlayer;
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

        if (elapsedSeconds >= 0) {
          var remainingSeconds = emotion.cooldownSeconds - elapsedSeconds;

          if (remainingSeconds > 0) {
            player.sendMessage("§cPlease wait another §4" + formatSecondsToTimeString(remainingSeconds) + " §cuntil playing the emotion §4" + args[0] + " §cagain!");
            return true;
          }
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
    var mismatchedPermission = false;

    for (var emotionEntry : config.rootSection.emotions.entrySet()) {
      var emotion = emotionEntry.getValue();
      var emotionIdentifier = emotionEntry.getKey();

      if (!(sender.hasPermission("configurableemotions.emotion." + emotionIdentifier.toLowerCase()))) {
        mismatchedPermission = true;
        continue;
      }

      helpScreenEntries.add(new HelpScreenEntry(
        emotionIdentifier,
        emotion.description.asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment),
        emotion.supportsSelf,
        emotion.supportsOthers,
        emotion.supportsAll
      ));
    }

    if (mismatchedPermission && helpScreenEntries.isEmpty()) {
      sender.sendMessage("§cYou do not have access to any available emotion!");
      return true;
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

      if (player.getName().equals(input) || sanitize(player.getDisplayName()).equals(input))
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
    var receivers = new ArrayList<Player>();
    var messageEnvironment = makeMessageEnvironment(sender);

    for (var receiver : Bukkit.getOnlinePlayers()) {
      // Avoid looping twice - send ahead of all other actions

      var receiverEnvironment = addReceiverVariablesAndBuild(receiver, messageEnvironment);

      if (emotion.messagesAllBroadcast != null)
        displayMessages(receiver, receiverEnvironment, emotion.messagesAllBroadcast);

      if (receiver.equals(sender))
        continue;

      receivers.add(receiver);

      if (emotion._soundReceiver != null)
        emotion._soundReceiver.play(receiver);

      if (emotion.messagesAllReceiver != null)
        displayMessages(receiver, receiverEnvironment, emotion.messagesAllReceiver);
    }

    if (receivers.isEmpty())
      return false;

    for (var senderEffect : emotion.effectsSender)
      effectPlayer.playEffect(senderEffect, List.of(sender));

    for (var receiverEffect : emotion.effectsReceiver)
      effectPlayer.playEffect(receiverEffect, receivers);

    if (emotion._soundSender != null)
      emotion._soundSender.play(sender);

    if (emotion.messagesAllSender != null)
      displayMessages(sender, messageEnvironment.build(), emotion.messagesAllSender);

    return true;
  }

  private IEvaluationEnvironment addReceiverVariablesAndBuild(Player receiver, EvaluationEnvironmentBuilder environment) {
    return environment
      .withStaticVariable("receiver_name", receiver.getName())
      .withStaticVariable("receiver_display_name", receiver.getDisplayName())
      .build();
  }

  private EvaluationEnvironmentBuilder makeMessageEnvironment(Player sender) {
    return config.rootSection.getBaseEnvironment()
      .withStaticVariable("sender_name", sender.getName())
      .withStaticVariable("sender_display_name", sender.getDisplayName());
  }

  private void displayMessages(Player receiver, IEvaluationEnvironment messageEnvironment, DisplayedMessages messages) {
    if (messages.actionBarMessage != null)
      messages.actionBarMessage.sendActionBarMessage(receiver, messageEnvironment);

    if (messages.chatMessage != null)
      messages.chatMessage.sendMessage(receiver, messageEnvironment);

    if (messages.titleMessage != null || messages.subTitleMessage != null) {
      var applicator = (messages.titleMessage == null ? messages.subTitleMessage : messages.titleMessage).applicator;

      applicator.sendTitles(
        receiver,
        messages.titleMessage, messageEnvironment,
        messages.subTitleMessage, messageEnvironment,
        messages.titleFadeIn,
        messages.titleStay,
        messages.titleFadeOut
      );
    }
  }

  private void playEmotionOther(Player sender, Player receiver, EmotionSection emotion) {
    var messageEnvironment = makeMessageEnvironment(sender);
    var receiverEnvironment = addReceiverVariablesAndBuild(receiver, messageEnvironment);

    if (emotion.messagesOneBroadcast != null) {
      for (var messageReceiver : Bukkit.getOnlinePlayers())
        displayMessages(messageReceiver, receiverEnvironment, emotion.messagesOneBroadcast);
    }

    if (emotion._soundReceiver != null)
      emotion._soundReceiver.play(receiver);

    if (emotion.messagesOneReceiver != null)
      displayMessages(receiver, receiverEnvironment, emotion.messagesOneReceiver);

    for (var senderEffect : emotion.effectsSender)
      effectPlayer.playEffect(senderEffect, List.of(sender));

    for (var receiverEffect : emotion.effectsReceiver)
      effectPlayer.playEffect(receiverEffect, List.of(receiver));

    if (emotion._soundSender != null)
      emotion._soundSender.play(sender);

    if (emotion.messagesOneSender != null)
      displayMessages(sender, messageEnvironment.build(), emotion.messagesOneSender);
  }

  private void playEmotionSelf(Player sender, EmotionSection emotion) {
    var messageEnvironment = makeMessageEnvironment(sender);

    if (emotion.messagesSelfBroadcast != null) {
      for (var messageReceiver : Bukkit.getOnlinePlayers())
        displayMessages(messageReceiver, messageEnvironment.build(), emotion.messagesSelfBroadcast);
    }

    for (var senderEffect : emotion.effectsSender)
      effectPlayer.playEffect(senderEffect, List.of(sender));

    if (emotion._soundSender != null)
      emotion._soundSender.play(sender);

    if (emotion.messagesSelfSender != null)
      displayMessages(sender, messageEnvironment.build(), emotion.messagesSelfSender);
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
      var displayName = sanitize(player.getDisplayName());

      if (!name.equals(displayName)) {
        if (!StringUtils.containsIgnoreCase(displayName, args[1]))
          continue;

        nameSuggestions.add(displayName);
        continue;
      }

      if (!StringUtils.containsIgnoreCase(name, args[1]))
        continue;

      nameSuggestions.add(name);
    }

    var allSentinel = config.rootSection.commands.emotion.allSentinel;

    if (emotion.supportsAll && StringUtils.containsIgnoreCase(allSentinel, args[1]))
      nameSuggestions.add(allSentinel);

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

  public static String sanitize(String input) {
    var inputLength = input.length();
    var result = new StringBuilder(inputLength);

    /*
      - Simple Colors: (§|&)[0-9a-fk-or]
      - Hex Colors: (§|&)#([0-9a-fA-F]{3} | [0-9a-fA-F]{6})
      - XML-Tags (Mini-Message)
        - May contain other tags in string-parameters, marked by " or '
        - Example: <hover:show_text:"<red>test:TEST">
        - Escape-Sequences: \", \', \< \>
     */

    int possibleTagBeginning = -1;
    var quoteStack = new Stack<Character>();

    char previousChar = 0;

    for (var i = 0; i < inputLength; ++i) {
      var currentChar = input.charAt(i);
      var isEscaped = previousChar == '\\';

      previousChar = currentChar;

      if (possibleTagBeginning >= 0) {
        if (!isEscaped && (currentChar == '"' || currentChar == '\'')) {
          if (!quoteStack.empty() && quoteStack.peek() == currentChar)
            quoteStack.pop();
          else
            quoteStack.push(currentChar);
        }

        // No need to step through tags in strings; just anticipate non-stringed close char
        if (!quoteStack.empty())
          continue;

        if (!isEscaped && currentChar == '>') {
          possibleTagBeginning = -1;
          continue;
        }

        continue;
      }

      var isLastChar = i == inputLength - 1;

      if (!isLastChar && (currentChar == '§' || currentChar == '&')) {
        var nextChar = input.charAt(i + 1);

        if (
          isAlphaNumeric(nextChar) ||
            (nextChar >= 'k' && nextChar <= 'o') ||
            nextChar == 'r'
        ) {
          ++i;
          continue;
        }

        if (nextChar == '#') {
          var remainingChars = inputLength - 1 - i;
          var maxMatchingChars = Math.min(6, remainingChars);

          var matchedChars = 0;

          for (; matchedChars < maxMatchingChars; ++matchedChars) {
            if (!isAlphaNumeric(input.charAt(1 + (matchedChars + 1))))
              break;
          }

          // Skips & (current), # (next; +1) and the number of matched alphanumerics, up to 6
          // I think I've seen shorthands - so that's why 3 or 6; leave &# or malformed as is
          if (matchedChars == 3 || matchedChars == 6) {
            i += matchedChars + 1;
            continue;
          }
        }
      }

      if (!isEscaped && currentChar == '<') {
        possibleTagBeginning = i;
        continue;
      }

      result.append(currentChar);
    }

    // Tag was never closed, so let's leave it in
    if (possibleTagBeginning >= 0)
      result.append(input.substring(possibleTagBeginning));

    return result.toString().trim();
  }

  private static boolean isAlphaNumeric(char c) {
    return (
      (c >= '0' && c <= '9') ||
        (c >= 'a' && c <= 'f') ||
        (c >= 'A' && c <= 'F')
    );
  }
}
