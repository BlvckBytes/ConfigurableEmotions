package me.blvckbytes.configurable_emotions.command;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.EffectPlayer;
import me.blvckbytes.configurable_emotions.UidScopedNamedStampStore;
import me.blvckbytes.configurable_emotions.config.DisplayedMessages;
import me.blvckbytes.configurable_emotions.config.EmotionSection;
import me.blvckbytes.configurable_emotions.config.MainSection;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.apache.commons.lang.ArrayUtils;
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
  private final UidScopedNamedStampStore stampStore;
  private final ConfigKeeper<MainSection> config;

  public EmotionCommand(
    EffectPlayer effectPlayer,
    UidScopedNamedStampStore stampStore,
    ConfigKeeper<MainSection> config
  ) {
    this.effectPlayer = effectPlayer;
    this.stampStore = stampStore;
    this.config = config;
  }

  // ================================================================================
  // Main Command-Invocation
  // ================================================================================

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      config.rootSection.playerMessages.playerOnlyCommand.sendMessage(sender, config.rootSection.builtBaseEnvironment);
      return true;
    }

    if (!CommandPermission.COMMAND_EMOTION.hasPermission(player)) {
      config.rootSection.playerMessages.missingPermissionEmotionCommand.sendMessage(player, config.rootSection.builtBaseEnvironment);
      return true;
    }

    if (args.length == 0) {
      displayOverviewScreen(player, label, 1);
      return true;
    }

    var identifier = args[0];
    Integer overviewPage;

    // While this is not necessarily the cleanest solution, I prefer making fully numeric
    // emotion-identifiers without arguments inaccessible above further increasing the command's complexity.
    if (args.length == 1 && (overviewPage = tryParseInteger(identifier)) != null) {
      displayOverviewScreen(player, label, overviewPage);
      return true;
    }

    var identifierLower = identifier.toLowerCase();
    var emotion = config.rootSection.emotionByIdentifierLower.get(identifierLower);

    if (emotion == null) {
      config.rootSection.playerMessages.unknownEmotionProvided.sendMessage(
        player,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("input", identifier)
          .build()
      );
      return true;
    }

    if (!CommandPermission.hasEmotionPermission(player, identifierLower)) {
      config.rootSection.playerMessages.missingEmotionPermission.sendMessage(
        player,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("emotion_identifier", identifier)
          .build()
      );
      return true;
    }

    if (emotion.cooldownSeconds != 0 && !CommandPermission.hasCooldownBypassPermission(player, identifierLower)) {
      var elapsedSeconds = getElapsedCooldownSeconds(identifierLower, player);

      if (elapsedSeconds >= 0) {
        var remainingSeconds = emotion.cooldownSeconds - elapsedSeconds;

        if (remainingSeconds > 0) {
          config.rootSection.playerMessages.awaitRemainingCooldown.sendMessage(
            player,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("remaining_time", formatSecondsToTimeString(remainingSeconds))
              .withStaticVariable("emotion_identifier", identifier)
              .build()
          );
          return true;
        }
      }
    }

    var isImplicitAllTarget = emotion.doesNoTargetEqualsAll && args.length == 1;

    if (args.length >= 2 || isImplicitAllTarget) {
      var firstEmotionTarget = isImplicitAllTarget ? config.rootSection.commands.emotion.allSentinel : args[1];

      if (firstEmotionTarget.equalsIgnoreCase(config.rootSection.commands.emotion.allSentinel)) {
        if (!emotion.supportsAll) {
          config.rootSection.playerMessages.unsupportedAllTarget.sendMessage(
            player,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("emotion_identifier", identifier)
              .build()
          );
          return true;
        }

        if (args.length > 2) {
          config.rootSection.playerMessages.cannotCombineAllSentinelWithNames.sendMessage(
            player,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("all_sentinel", config.rootSection.commands.emotion.allSentinel)
              .build()
          );
          return true;
        }

        if (!playEmotionAll(player, emotion)) {
          config.rootSection.playerMessages.noReceivingPlayersOnline.sendMessage(
            player,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("emotion_identifier", identifier)
              .build()
          );
          return true;
        }

        if (emotion.cooldownSeconds != 0)
          touchLastExecutionStamp(identifierLower, player);

        return true;
      }

      if (!emotion.supportsOthers) {
        config.rootSection.playerMessages.unsupportedOtherTarget.sendMessage(
          player,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("emotion_identifier", identifier)
            .build()
        );
        return true;
      }

      var targetPlayers = new HashSet<Player>();

      for (var i = 1; i < args.length; ++i) {
        var targetName = args[i];

        if (config.rootSection.commands.emotion.allSentinel.equalsIgnoreCase(targetName)) {
          config.rootSection.playerMessages.cannotCombineAllSentinelWithNames.sendMessage(
            player,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("all_sentinel", config.rootSection.commands.emotion.allSentinel)
              .build()
          );
          return true;
        }

        var targetPlayer = getPlayerByNameOrDisplayName(targetName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
          config.rootSection.playerMessages.receivingPlayerNotOnline.sendMessage(
            player,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("target_player", targetName)
              .build()
          );
          return true;
        }

        if (targetPlayer.equals(player)) {
          config.rootSection.playerMessages.receiverCannotBeSelf.sendMessage(player, config.rootSection.builtBaseEnvironment);
          return true;
        }

        if (!targetPlayers.add(targetPlayer)) {
          config.rootSection.playerMessages.receivingPlayerDuplicate.sendMessage(
            player,
            config.rootSection.getBaseEnvironment()
              .withStaticVariable("target_player", targetName)
              .build()
          );
          return true;
        }
      }

      if (targetPlayers.size() == 1) {
        playEmotionOther(player, targetPlayers.iterator().next(), emotion);

        if (emotion.cooldownSeconds != 0)
          touchLastExecutionStamp(identifierLower, player);

        return true;
      }

      if (targetPlayers.size() > emotion.maximumNumberOfTargets) {
        config.rootSection.playerMessages.maximumNumberOfTargetsExceeded.sendMessage(
          player,
          config.rootSection.getBaseEnvironment()
            .withStaticVariable("emotion_identifier", identifier)
            .withStaticVariable("maximum_count", emotion.maximumNumberOfTargets)
            .build()
        );
        return true;
      }

      playEmotionMany(player, targetPlayers, emotion);

      if (emotion.cooldownSeconds != 0)
        touchLastExecutionStamp(identifierLower, player);

      return true;
    }

    if (!emotion.supportsSelf) {
      config.rootSection.playerMessages.unsupportedPlayingOnSelf.sendMessage(
        player,
        config.rootSection.getBaseEnvironment()
          .withStaticVariable("emotion_identifier", args[0])
          .build()
      );
      return true;
    }

    playEmotionSelf(player, emotion);

    if (emotion.cooldownSeconds != 0)
      touchLastExecutionStamp(identifierLower, player);

    return true;
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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

    if (args.length - 1 > emotion.maximumNumberOfTargets)
      return List.of();

    var allSentinel = config.rootSection.commands.emotion.allSentinel;

    // The all-sentinel may not be followed up by any additional names, as that would be illogical
    if (args[1].equalsIgnoreCase(allSentinel))
      return List.of();

    var nameSuggestions = new ArrayList<String>();
    var lastArg = args[args.length - 1];

    for (var player : Bukkit.getOnlinePlayers()) {
      if (player.equals(sender))
        continue;

      var name = player.getName();
      var displayName = sanitize(player.getDisplayName());

      if (!name.equals(displayName)) {
        if (!StringUtils.containsIgnoreCase(displayName, lastArg))
          continue;

        if (doesArgsTargetListContainIgnoreCase(args, displayName))
          continue;

        nameSuggestions.add(displayName);
        continue;
      }

      if (!StringUtils.containsIgnoreCase(name, lastArg))
        continue;

      if (doesArgsTargetListContainIgnoreCase(args, name))
        continue;

      nameSuggestions.add(name);
    }

    // Only suggest the all-sentinel on the first target-name
    if (emotion.supportsAll && args.length == 2 && StringUtils.containsIgnoreCase(allSentinel, args[1]))
      nameSuggestions.add(allSentinel);

    return nameSuggestions;
  }

  // ================================================================================
  // Direct Command-Invocation (shorthands)
  // ================================================================================

  public boolean onDirectCommand(
    String identifierLower,
    @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args
  ) {
    return onCommand(sender, command, label, (String[]) ArrayUtils.add(args, 0, identifierLower));
  }

  public List<String> onDirectTabComplete(
    String identifierLower,
    @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args
  ) {
    return onTabComplete(sender, command, label, (String[]) ArrayUtils.add(args, 0, identifierLower));
  }

  // ================================================================================
  // Utilities
  // ================================================================================

  private void displayOverviewScreen(Player player, String commandLabel, int page) {
    List<HelpScreenEntry> helpScreenEntries = new ArrayList<>();
    var mismatchedPermission = false;

    for (var emotionEntry : config.rootSection.emotions.entrySet()) {
      var emotion = emotionEntry.getValue();
      var emotionIdentifier = emotionEntry.getKey();

      if (!(player.hasPermission("configurableemotions.emotion." + emotionIdentifier.toLowerCase()))) {
        mismatchedPermission = true;
        continue;
      }

      var aliases = new ArrayList<>(emotion.directAliases);

      if (emotion.tryRegisterDirectly)
        aliases.add(emotionIdentifier.toLowerCase());

      helpScreenEntries.add(new HelpScreenEntry(
        emotionIdentifier,
        emotion.description.asScalar(ScalarType.STRING, config.rootSection.builtBaseEnvironment),
        aliases,
        emotion.supportsSelf,
        emotion.supportsOthers,
        emotion.supportsAll
      ));
    }

    if (mismatchedPermission && helpScreenEntries.isEmpty()) {
      config.rootSection.playerMessages.noAccessToAnyEmotion.sendMessage(player, config.rootSection.builtBaseEnvironment);
      return;
    }

    int pageSize = config.rootSection.commands.emotion.paginationSize;
    int numberOfPages = (helpScreenEntries.size() + (pageSize - 1)) / pageSize;

    if (page > numberOfPages)
      page = numberOfPages;

    if (page <= 0)
      page = 1;

    if (helpScreenEntries.size() > pageSize) {
      var firstIndex = (page - 1) * pageSize;
      var lastIndex = Math.min(helpScreenEntries.size(), firstIndex + pageSize);
      helpScreenEntries = helpScreenEntries.subList(firstIndex, lastIndex);
    }

    config.rootSection.playerMessages.commandEmotionHelpScreen.sendMessage(
      player,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("number_of_pages", numberOfPages)
        .withStaticVariable("current_page", page)
        .withStaticVariable("page_size", pageSize)
        .withStaticVariable("label", commandLabel)
        .withStaticVariable("all_sentinel", config.rootSection.commands.emotion.allSentinel)
        .withStaticVariable("emotions", helpScreenEntries)
        .build()
    );
  }

  private boolean doesArgsTargetListContainIgnoreCase(String[] args, String name) {
    if (args.length < 2)
      return false;

    for (var i = 1; i < args.length; ++i) {
      if (args[i].equalsIgnoreCase(name))
        return true;
    }

    return false;
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

    if (Bukkit.getOnlinePlayers().size() == 1)
      return false;

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

  private void playEmotionMany(Player sender, Collection<Player> receivers, EmotionSection emotion) {
    var receiverNames = new ArrayList<String>(receivers.size());
    var receiverDisplayNames = new ArrayList<String>(receivers.size());

    for (var receiver : receivers) {
      receiverNames.add(receiver.getName());
      receiverDisplayNames.add(receiver.getDisplayName());
    }

    var messageEnvironment = makeMessageEnvironment(sender)
      .withStaticVariable("receivers_names", receiverNames)
      .withStaticVariable("receivers_display_names", receiverDisplayNames);

    for (var receiver : Bukkit.getOnlinePlayers()) {
      // Avoid looping twice - send ahead of all other actions

      var receiverEnvironment = addReceiverVariablesAndBuild(receiver, messageEnvironment);

      if (emotion.messagesManyBroadcast != null)
        displayMessages(receiver, receiverEnvironment, emotion.messagesManyBroadcast);

      if (receiver.equals(sender))
        continue;

      receivers.add(receiver);

      if (emotion._soundReceiver != null)
        emotion._soundReceiver.play(receiver);

      if (emotion.messagesManyReceiver != null)
        displayMessages(receiver, receiverEnvironment, emotion.messagesManyReceiver);
    }

    for (var senderEffect : emotion.effectsSender)
      effectPlayer.playEffect(senderEffect, List.of(sender));

    for (var receiverEffect : emotion.effectsReceiver)
      effectPlayer.playEffect(receiverEffect, receivers);

    if (emotion._soundSender != null)
      emotion._soundSender.play(sender);

    if (emotion.messagesManySender != null)
      displayMessages(sender, messageEnvironment.build(), emotion.messagesManySender);
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

  private void touchLastExecutionStamp(String identifierLower, Player player) {
    stampStore.write(player.getUniqueId(), identifierLower, System.currentTimeMillis());
  }

  private int getElapsedCooldownSeconds(String identifierLower, Player player) {
    var lastExecutionStamp = stampStore.read(player.getUniqueId(), identifierLower);

    if (lastExecutionStamp < 0)
      return -1;

    return (int) Math.round((System.currentTimeMillis() - lastExecutionStamp) / 1000.0);
  }

  private String sanitize(String input) {
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

  private boolean isAlphaNumeric(char c) {
    return (
      (c >= '0' && c <= '9') ||
        (c >= 'a' && c <= 'f') ||
        (c >= 'A' && c <= 'F')
    );
  }

  private @Nullable Integer tryParseInteger(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
