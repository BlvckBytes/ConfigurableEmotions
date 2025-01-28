package me.blvckbytes.configurable_emotions;

import com.cryptomorin.xseries.XMaterial;
import me.blvckbytes.bukkitevaluable.CommandUpdater;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.configurable_emotions.command.CommandPermission;
import me.blvckbytes.configurable_emotions.command.EmotionCommand;
import me.blvckbytes.configurable_emotions.command.EmotionReloadCommand;
import me.blvckbytes.configurable_emotions.config.*;
import me.blvckbytes.configurable_emotions.listener.CommandSendListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ConfigurableEmotionsPlugin extends JavaPlugin {

  private UidScopedNamedStampStore stampStore;

  @Override
  public void onEnable() {
    var logger = getLogger();

    try {
      // First invocation is quite heavy - warm up cache
      XMaterial.matchXMaterial(Material.AIR);

      var configManager = new ConfigManager(this, "config");
      var config = new ConfigKeeper<>(configManager, "config.yml", MainSection.class);
      var commandUpdater = new CommandUpdater(this);

      stampStore = new UidScopedNamedStampStore(this, logger);

      var effectPlayer = new EffectPlayer(this, logger);
      var emotionCommand = Objects.requireNonNull(getCommand(EmotionCommandSection.INITIAL_NAME));
      var emotionCommandHandler = new EmotionCommand(effectPlayer, stampStore, config);
      emotionCommand.setExecutor(emotionCommandHandler);

      var emotionReloadCommand = Objects.requireNonNull(getCommand(EmotionReloadCommandSection.INITIAL_NAME));
      emotionReloadCommand.setExecutor(new EmotionReloadCommand(config, logger));

      var commandSendListener = new CommandSendListener(this, config);
      Bukkit.getServer().getPluginManager().registerEvents(commandSendListener, this);

      var previouslyRegisteredDirectCommands = new ArrayList<Command>();

      Runnable updateCommands = () -> {
        config.rootSection.commands.emotion.apply(emotionCommand, commandUpdater);
        config.rootSection.commands.emotionReload.apply(emotionReloadCommand, commandUpdater);

        for (var commandIterator = previouslyRegisteredDirectCommands.iterator(); commandIterator.hasNext();) {
          var previousCommand = commandIterator.next();

          commandUpdater.tryUnregisterCommand(previousCommand);
          commandSendListener.unregisterPluginCommand(previousCommand);

          commandIterator.remove();
        }

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
              previouslyRegisteredDirectCommands.add(directCommand);
              commandSendListener.registerPluginCommand(directCommand, sender -> CommandPermission.hasEmotionPermission(sender, identifierLower));
            }
          }
        }

        commandUpdater.trySyncCommands();
      };

      updateCommands.run();
      config.registerReloadListener(updateCommands);

      Runnable emotionCountLogger = () -> logger.info("Loaded " + config.rootSection.emotions.size() + " configured emotions!");

      emotionCountLogger.run();
      config.registerReloadListener(emotionCountLogger);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not initialize plugin", e);
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    if (stampStore != null)
      stampStore.onDisable();
  }
}
