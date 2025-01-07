package me.blvckbytes.configurable_emotions;

import com.cryptomorin.xseries.XMaterial;
import me.blvckbytes.bukkitevaluable.CommandUpdater;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.configurable_emotions.command.EmotionCommand;
import me.blvckbytes.configurable_emotions.command.EmotionReloadCommand;
import me.blvckbytes.configurable_emotions.config.*;
import me.blvckbytes.configurable_emotions.listener.CommandSendListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public class ConfigurableEmotionsPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    var logger = getLogger();

    try {
      // First invocation is quite heavy - warm up cache
      XMaterial.matchXMaterial(Material.AIR);

      var configManager = new ConfigManager(this, "config");
      var config = new ConfigKeeper<>(configManager, "config.yml", MainSection.class);
      var commandUpdater = new CommandUpdater(this);

      var effectPlayer = new EffectPlayer(this);
      var emotionCommand = Objects.requireNonNull(getCommand(EmotionCommandSection.INITIAL_NAME));
      emotionCommand.setExecutor(new EmotionCommand(effectPlayer, config));

      var emotionReloadCommand = Objects.requireNonNull(getCommand(EmotionReloadCommandSection.INITIAL_NAME));
      emotionReloadCommand.setExecutor(new EmotionReloadCommand(config, logger));

      Runnable updateCommands = () -> {
        config.rootSection.commands.emotion.apply(emotionCommand, commandUpdater);
        config.rootSection.commands.emotionReload.apply(emotionReloadCommand, commandUpdater);

        commandUpdater.trySyncCommands();
      };

      updateCommands.run();
      config.registerReloadListener(updateCommands);

      Runnable emotionCountLogger = () -> logger.info("Loaded " + config.rootSection.emotions.size() + " configured emotions!");

      emotionCountLogger.run();
      config.registerReloadListener(emotionCountLogger);

      Bukkit.getServer().getPluginManager().registerEvents(new CommandSendListener(this, config), this);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not initialize plugin", e);
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }
}
