package me.blvckbytes.configurable_emotions.command.emotion;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import me.blvckbytes.configurable_emotions.EffectPlayer;
import me.blvckbytes.configurable_emotions.UidScopedNamedStampStore;
import me.blvckbytes.configurable_emotions.command.CommandPermission;
import me.blvckbytes.configurable_emotions.config.DisplayedMessages;
import me.blvckbytes.configurable_emotions.config.EmotionSection;
import me.blvckbytes.configurable_emotions.config.MainSection;
import me.blvckbytes.configurable_emotions.discord.DiscordApi;
import me.blvckbytes.configurable_emotions.discord.DiscordApiManager;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileFlag;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

public class EmotionCommand implements CommandExecutor, TabCompleter {

  private final EffectPlayer effectPlayer;
  private final UidScopedNamedStampStore stampStore;
  private final PlayerProfileStore profileStore;
  private final DiscordApiManager discordApiManager;
  private final ConfigKeeper<MainSection> config;

  public EmotionCommand(
    EffectPlayer effectPlayer,
    UidScopedNamedStampStore stampStore,
    PlayerProfileStore profileStore,
    DiscordApiManager discordApiManager,
    ConfigKeeper<MainSection> config
  ) {
    this.effectPlayer = effectPlayer;
    this.stampStore = stampStore;
    this.profileStore = profileStore;
    this.discordApiManager = discordApiManager;
    this.config = config;
  }

  // ================================================================================
  // Main Command-Invocation
  // ================================================================================

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player player)) {
      config.rootSection.playerMessages.playerOnlyCommand.sendMessage(sender);
      return true;
    }

    if (!CommandPermission.COMMAND_EMOTION.hasPermission(player)) {
      config.rootSection.playerMessages.missingPermissionEmotionCommand.sendMessage(player);
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
        new InterpretationEnvironment()
          .withVariable("input", identifier)
      );
      return true;
    }

    if (!CommandPermission.hasEmotionPermission(player, identifierLower)) {
      config.rootSection.playerMessages.missingEmotionPermission.sendMessage(
        player,
        new InterpretationEnvironment()
          .withVariable("emotion_identifier", identifier)
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
            new InterpretationEnvironment()
              .withVariable("remaining_time", formatSecondsToTimeString(remainingSeconds))
              .withVariable("emotion_identifier", identifier)
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
            new InterpretationEnvironment()
              .withVariable("emotion_identifier", identifier)
          );
          return true;
        }

        if (args.length > 2) {
          config.rootSection.playerMessages.cannotCombineAllSentinelWithNames.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("all_sentinel", config.rootSection.commands.emotion.allSentinel)
          );
          return true;
        }

        if (!playEmotionAll(player, emotion)) {
          config.rootSection.playerMessages.noReceivingPlayersOnline.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("emotion_identifier", identifier)
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
          new InterpretationEnvironment()
            .withVariable("emotion_identifier", identifier)
        );
        return true;
      }

      var targetPlayers = new HashSet<Player>();

      for (var i = 1; i < args.length; ++i) {
        var targetName = args[i];

        if (config.rootSection.commands.emotion.allSentinel.equalsIgnoreCase(targetName)) {
          config.rootSection.playerMessages.cannotCombineAllSentinelWithNames.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("all_sentinel", config.rootSection.commands.emotion.allSentinel)
          );
          return true;
        }

        var targetPlayer = getPlayerByNameOrDisplayName(player, targetName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
          config.rootSection.playerMessages.receivingPlayerNotOnline.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("target_player", targetName)
          );
          return true;
        }

        if (targetPlayer.equals(player)) {
          config.rootSection.playerMessages.receiverCannotBeSelf.sendMessage(player);
          return true;
        }

        if (!targetPlayers.add(targetPlayer)) {
          config.rootSection.playerMessages.receivingPlayerDuplicate.sendMessage(
            player,
            new InterpretationEnvironment()
              .withVariable("target_player", targetName)
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
          new InterpretationEnvironment()
            .withVariable("emotion_identifier", identifier)
            .withVariable("maximum_count", emotion.maximumNumberOfTargets)
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
        new InterpretationEnvironment()
          .withVariable("emotion_identifier", args[0])
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
    if (!(sender instanceof Player player))
      return List.of();

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

    for (var target : Bukkit.getOnlinePlayers()) {
      if (target.equals(player))
        continue;

      if (!player.canSee(target) && !player.hasPermission("configurableemotions.bypass-hidden"))
        continue;

      var name = target.getName();
      var displayName = sanitize(target.getDisplayName());

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
    return onCommand(sender, command, label, ArrayUtils.add(args, identifierLower));
  }

  public List<String> onDirectTabComplete(
    String identifierLower,
    @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args
  ) {
    return onTabComplete(sender, command, label, ArrayUtils.add(args, identifierLower));
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
        emotion.description.interpret(SlotType.SINGLE_LINE_CHAT, null).get(0),
        aliases,
        emotion.supportsSelf,
        emotion.supportsOthers,
        emotion.supportsAll
      ));
    }

    if (mismatchedPermission && helpScreenEntries.isEmpty()) {
      config.rootSection.playerMessages.noAccessToAnyEmotion.sendMessage(player);
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
      new InterpretationEnvironment()
        .withVariable("number_of_pages", numberOfPages)
        .withVariable("current_page", page)
        .withVariable("page_size", pageSize)
        .withVariable("label", commandLabel)
        .withVariable("all_sentinel", config.rootSection.commands.emotion.allSentinel)
        .withVariable("emotions", helpScreenEntries)
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

  private @Nullable Player getPlayerByNameOrDisplayName(Player sender, String input) {
    for (var target : Bukkit.getOnlinePlayers()) {
      if (!sender.canSee(target) && !sender.hasPermission("configurableemotions.bypass-hidden"))
        continue;

      if (target.getName().equals(input) || sanitize(target.getDisplayName()).equals(input))
        return target;
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
    if (Bukkit.getOnlinePlayers().size() == 1)
      return false;

    var receivers = new ArrayList<Player>();
    var messageEnvironment = makeMessageEnvironment(sender);
    var messages = emotion.accessAtAllMessages();

    for (var receiver : Bukkit.getOnlinePlayers()) {
      // Avoid looping twice - send ahead of all other actions

      var isSender = receiver.equals(sender);

      addReceiverVariables(receiver, messageEnvironment);

      if (messages.asBroadcast != null)
        displayMessages(receiver, isSender, messageEnvironment, messages.asBroadcast);

      if (isSender)
        continue;

      receivers.add(receiver);

      playEmotionSound(emotion, true, receiver);

      if (messages.toReceiver != null)
        displayMessages(receiver, false, messageEnvironment, messages.toReceiver);
    }

    if (messages.asBroadcast != null)
      possiblyBroadcastToConsole(emotion, messages.asBroadcast, messageEnvironment);

    for (var effect : emotion.effects) {
      effectPlayer.playEffect(effect, false, List.of(sender));
      effectPlayer.playEffect(effect, true, receivers);
    }

    playEmotionSound(emotion, false, sender);

    if (messages.toSender != null)
      displayMessages(sender, true, messageEnvironment, messages.toSender);

    DiscordApi discordApi;

    if (messages.toDiscord != null && (discordApi = discordApiManager.getApi()) != null)
      discordApi.sendMessage(messages.toDiscord.asPlainString(messageEnvironment));

    return true;
  }

  private void playEmotionMany(Player sender, Collection<Player> receivers, EmotionSection emotion) {
    var receiverNames = new ArrayList<String>(receivers.size());
    var receiverDisplayNames = new ArrayList<Component>(receivers.size());
    var messages = emotion.accessAtManyMessages();

    for (var receiver : receivers) {
      receiverNames.add(receiver.getName());
      receiverDisplayNames.add(receiver.displayName());
    }

    var messageEnvironment = makeMessageEnvironment(sender)
      .withVariable("receivers_names", receiverNames)
      .withVariable("receivers_display_names", receiverDisplayNames);

    if (messages.asBroadcast != null) {
      for (var broadcastReceiver : Bukkit.getOnlinePlayers())
        displayMessages(broadcastReceiver, sender.equals(broadcastReceiver) || receivers.contains(broadcastReceiver), messageEnvironment, messages.asBroadcast);

      possiblyBroadcastToConsole(emotion, messages.asBroadcast, messageEnvironment);
    }

    for (var receiver : receivers) {
      addReceiverVariables(receiver, messageEnvironment);

      playEmotionSound(emotion, false, receiver);

      if (messages.toReceiver != null)
        displayMessages(receiver, true, messageEnvironment, messages.toReceiver);
    }

    for (var effect : emotion.effects) {
      effectPlayer.playEffect(effect, false, List.of(sender));
      effectPlayer.playEffect(effect, true, receivers);
    }

    playEmotionSound(emotion, false, sender);

    if (messages.toSender != null)
      displayMessages(sender, true, messageEnvironment, messages.toSender);

    DiscordApi discordApi;

    if (messages.toDiscord != null && (discordApi = discordApiManager.getApi()) != null)
      discordApi.sendMessage(messages.toDiscord.asPlainString(messageEnvironment));
  }

  private void addReceiverVariables(Player receiver, InterpretationEnvironment environment) {
    environment
      .withVariable("receiver_name", receiver.getName())
      .withVariable("receiver_display_name", receiver.displayName());
  }

  private InterpretationEnvironment makeMessageEnvironment(Player sender) {
    return new InterpretationEnvironment()
      .withVariable("sender_name", sender.getName())
      .withVariable("sender_display_name", sender.displayName());
  }

  private void displayMessages(
    Player receiver,
    boolean isTargetedByEmotion,
    InterpretationEnvironment messageEnvironment,
    DisplayedMessages messages
  ) {
    var profile = profileStore.getProfile(receiver);

    if (messages.actionBarMessage != null && profile.getFlagOrDefault(PlayerProfileFlag.ACTION_BAR_ENABLED).doesShow(isTargetedByEmotion))
      messages.actionBarMessage.sendActionBar(receiver, messageEnvironment);

    if (messages.chatMessage != null && profile.getFlagOrDefault(PlayerProfileFlag.CHAT_ENABLED).doesShow(isTargetedByEmotion))
      messages.chatMessage.sendMessage(receiver, messageEnvironment);

    if (profile.getFlagOrDefault(PlayerProfileFlag.TITLE_ENABLED).doesShow(isTargetedByEmotion)) {
      if (messages.titleMessage != null || messages.subTitleMessage != null) {
        if (messages.titleMessage != null)
          receiver.sendTitlePart(TitlePart.TITLE, messages.titleMessage.interpret(SlotType.SINGLE_LINE_CHAT, messageEnvironment).get(0));

        if (messages.subTitleMessage != null)
          receiver.sendTitlePart(TitlePart.SUBTITLE, messages.subTitleMessage.interpret(SlotType.SINGLE_LINE_CHAT, messageEnvironment).get(0));

        receiver.sendTitlePart(
          TitlePart.TIMES,
          Title.Times.times(
            Duration.ofMillis(messages.titleFadeIn * 50L),
            Duration.ofMillis(messages.titleStay * 50L),
            Duration.ofMillis(messages.titleFadeOut * 50L)
          )
        );
      }
    }
  }

  private void playEmotionSound(EmotionSection emotion, boolean isBroadcast, Player player) {
    if (emotion._sound == null)
      return;

    // As of now, sounds only play for sender/receiver, so they're always the target of the emotion
    if (profileStore.getProfile(player).getFlagOrDefault(PlayerProfileFlag.SOUND_ENABLED).doesShow(!isBroadcast))
      emotion._sound.play(player, emotion._soundVolume, emotion._soundPitch);
  }

  private void playEmotionOther(Player sender, Player receiver, EmotionSection emotion) {
    var messageEnvironment = makeMessageEnvironment(sender);
    addReceiverVariables(receiver, messageEnvironment);
    var messages = emotion.accessAtOneMessages();

    if (messages.asBroadcast != null) {
      for (var messageReceiver : Bukkit.getOnlinePlayers())
        displayMessages(messageReceiver, messageReceiver.equals(receiver) || messageReceiver.equals(sender), messageEnvironment, messages.asBroadcast);

      possiblyBroadcastToConsole(emotion, messages.asBroadcast, messageEnvironment);
    }

    playEmotionSound(emotion, false, receiver);

    if (messages.toReceiver != null)
      displayMessages(receiver, true, messageEnvironment, messages.toReceiver);

    for (var effect : emotion.effects) {
      effectPlayer.playEffect(effect, false, List.of(sender));
      effectPlayer.playEffect(effect, false, List.of(receiver));
    }

    playEmotionSound(emotion, false, sender);

    if (messages.toSender != null)
      displayMessages(sender, true, messageEnvironment, messages.toSender);

    DiscordApi discordApi;

    if (messages.toDiscord != null && (discordApi = discordApiManager.getApi()) != null)
      discordApi.sendMessage(messages.toDiscord.asPlainString(messageEnvironment));
  }

  private void playEmotionSelf(Player sender, EmotionSection emotion) {
    var messageEnvironment = makeMessageEnvironment(sender);
    var messages = emotion.accessAtSelfMessages();

    if (messages.asBroadcast != null) {
      for (var messageReceiver : Bukkit.getOnlinePlayers())
        displayMessages(messageReceiver, messageReceiver.equals(sender), messageEnvironment, messages.asBroadcast);

      possiblyBroadcastToConsole(emotion, messages.asBroadcast, messageEnvironment);
    }

    for (var effect : emotion.effects)
      effectPlayer.playEffect(effect, false, List.of(sender));

    playEmotionSound(emotion, false, sender);

    if (messages.toSender != null)
      displayMessages(sender, true, messageEnvironment, messages.toSender);

    DiscordApi discordApi;

    if (messages.toDiscord != null && (discordApi = discordApiManager.getApi()) != null)
      discordApi.sendMessage(messages.toDiscord.asPlainString(messageEnvironment));
  }

  private void possiblyBroadcastToConsole(
    EmotionSection emotion,
    @Nullable DisplayedMessages broadcastMessages,
    InterpretationEnvironment messageEnvironment
  ) {
    if (broadcastMessages == null || !emotion.broadcastToConsole)
      return;

    if (broadcastMessages.chatMessage == null)
      return;

    broadcastMessages.chatMessage.sendMessage(Bukkit.getConsoleSender(), messageEnvironment);
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
