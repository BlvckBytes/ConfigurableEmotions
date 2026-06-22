package me.blvckbytes.configurable_emotions;

import at.blvckbytes.cm_mapper.ConfigHandler;
import at.blvckbytes.cm_mapper.ConfigKeeper;
import at.blvckbytes.cm_mapper.ConfigKeeperReloadEvent;
import at.blvckbytes.cm_mapper.section.command.CommandUpdater;
import com.cryptomorin.xseries.XMaterial;
import me.blvckbytes.configurable_emotions.command.CommandPermission;
import me.blvckbytes.configurable_emotions.command.emotion.EmotionCommand;
import me.blvckbytes.configurable_emotions.command.emotion_control.EmotionControlCommand;
import me.blvckbytes.configurable_emotions.config.*;
import me.blvckbytes.configurable_emotions.discord.DiscordApiManager;
import me.blvckbytes.configurable_emotions.listener.CommandSendListener;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileStore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ConfigurableEmotionsPlugin extends JavaPlugin implements Listener {

  private final List<Command> currentlyRegisteredDirectCommands = new ArrayList<>();

  private @Nullable UidScopedNamedStampStore stampStore;
  private @Nullable CommandUpdater commandUpdater;
  private @Nullable CommandSendListener commandSendListener;
  private @Nullable PlayerProfileStore profileStore;
  private @Nullable ConfigKeeper<MainSection> config;
  private Runnable configReloadRunnable;

  @Override
  public void onEnable() {
    var logger = getLogger();

    try {
      // First invocation is quite heavy - warm up cache
      XMaterial.matchXMaterial(Material.AIR);

      var configHandler = new ConfigHandler(this, "config");
      config = new ConfigKeeper<>(configHandler, "config.yml", MainSection.class);
      commandUpdater = new CommandUpdater(this);

      stampStore = new UidScopedNamedStampStore(this, logger);
      profileStore = new PlayerProfileStore(this, config, logger);

      var effectPlayer = new EffectPlayer(profileStore, this, logger);
      var emotionCommand = Objects.requireNonNull(getCommand(EmotionCommandSection.INITIAL_NAME));

      var discordApiManager = new DiscordApiManager(this, logger, config);
      getServer().getPluginManager().registerEvents(discordApiManager, this);

      var emotionCommandHandler = new EmotionCommand(effectPlayer, stampStore, profileStore, discordApiManager, config);
      emotionCommand.setExecutor(emotionCommandHandler);

      var emotionControlCommand = Objects.requireNonNull(getCommand(EmotionControlCommandSection.INITIAL_NAME));
      emotionControlCommand.setExecutor(new EmotionControlCommand(profileStore, config, logger));

      commandSendListener = new CommandSendListener(this, config);
      Bukkit.getServer().getPluginManager().registerEvents(commandSendListener, this);

      Runnable updateCommands = () -> {
        config.rootSection.commands.emotion.apply(emotionCommand, commandUpdater);
        config.rootSection.commands.emotionControl.apply(emotionControlCommand, commandUpdater);

        unregisterCurrentlyRegisteredDirectCommands();

        for (var identifierLower : config.rootSection.emotionByIdentifierLower.keySet()) {
          var emotion = config.rootSection.emotionByIdentifierLower.get(identifierLower);

          if (!emotion.tryRegisterDirectly)
            continue;

          var commandIdentifiers = new HashSet<String>();
          commandIdentifiers.add(identifierLower);
          commandIdentifiers.addAll(emotion.directAliases);

          for (var commandIdentifier : commandIdentifiers) {
            var directCommand = new Command(commandIdentifier) {

              @Override
              public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
                return emotionCommandHandler.onDirectCommand(identifierLower, sender, this, label, args);
              }

              @Override
              public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) throws IllegalArgumentException {
                return emotionCommandHandler.onDirectTabComplete(identifierLower, sender, this, label, args);
              }
            };

            if (commandUpdater.tryRegisterCommand(directCommand)) {
              currentlyRegisteredDirectCommands.add(directCommand);
              commandSendListener.registerPluginCommand(directCommand, sender -> CommandPermission.hasEmotionPermission(sender, identifierLower));
            }
          }
        }

        commandUpdater.trySyncCommands();
      };

      updateCommands.run();

      Runnable emotionCountLogger = () -> logger.info("Loaded " + config.rootSection.emotions.size() + " configured emotions!");

      emotionCountLogger.run();

      configReloadRunnable = () -> {
        updateCommands.run();
        emotionCountLogger.run();
      };

      getServer().getPluginManager().registerEvents(this, this);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not initialize plugin", e);
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  @EventHandler
  public void onConfigReload(ConfigKeeperReloadEvent event) {
    if (event.configKeeper == config && configReloadRunnable != null)
      configReloadRunnable.run();
  }

  @Override
  public void onDisable() {
    if (stampStore != null) {
      catchAll(stampStore::onDisable);
      stampStore = null;
    }

    if (profileStore != null) {
      catchAll(profileStore::onDisable);
      profileStore = null;
    }

    unregisterCurrentlyRegisteredDirectCommands();
  }

  private void catchAll(Runnable runnable) {
    try {
      runnable.run();
    } catch (Throwable e) {
      getLogger().log(Level.SEVERE, "An error occurred while trying to shut down", e);
    }
  }

  private void unregisterCurrentlyRegisteredDirectCommands() {
    for (var commandIterator = currentlyRegisteredDirectCommands.iterator(); commandIterator.hasNext();) {
      var previousCommand = commandIterator.next();

      if (commandUpdater != null)
        commandUpdater.tryUnregisterCommand(previousCommand);

      if (commandSendListener != null)
        commandSendListener.unregisterPluginCommand(previousCommand);

      commandIterator.remove();
    }
  }
}
