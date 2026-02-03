package me.blvckbytes.configurable_emotions.discord;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import me.blvckbytes.configurable_emotions.config.MainSection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class DiscordApiManager {

  private final Plugin plugin;
  private final Logger logger;
  private final ConfigKeeper<MainSection> config;

  private @Nullable EssentialsDiscordApi essentialsCache;
  private @Nullable DiscordApi discordApi;

  public DiscordApiManager(
    Plugin plugin,
    Logger logger,
    ConfigKeeper<MainSection> config
  ) {
    this.plugin = plugin;
    this.logger = logger;
    this.config = config;

    determineDiscordApi();
    this.config.registerReloadListener(this::determineDiscordApi);
  }

  public @Nullable DiscordApi getApi() {
    return this.discordApi;
  }

  private void determineDiscordApi() {
    var pluginManager = Bukkit.getServer().getPluginManager();

    Plugin dependency;

    if ((dependency = pluginManager.getPlugin("EssentialsDiscord")) != null && dependency.isEnabled()) {
      if (config.rootSection.discord.essentialsDiscord.enabled) {
        if (this.essentialsCache == null) {
          this.essentialsCache = new EssentialsDiscordApi(plugin, logger, config);
          logger.info("Successfully hooked into the EssentialsDiscord-API!");
        }
        else
          this.essentialsCache.onConfigReload();

        this.discordApi = this.essentialsCache;
        return;
      }
    }

    logger.info("Did not hook into any Discord-API (either none present or none enabled)!");
  }
}
