package me.blvckbytes.configurable_emotions.discord;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.config.MainSection;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discord.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class EssentialsDiscordApi implements DiscordApi {

  private final Plugin plugin;
  private final Logger logger;
  private final ConfigKeeper<MainSection> config;

  private final DiscordService discordService;

  private @Nullable MessageType messageType;

  public EssentialsDiscordApi(Plugin plugin, Logger logger, ConfigKeeper<MainSection> config) {
    this.discordService = Bukkit.getServicesManager().load(DiscordService.class);

    if (this.discordService == null)
      throw new IllegalStateException("Could not get a reference to EssentialsX's discord-service!");

    this.plugin = plugin;
    this.logger = logger;
    this.config = config;

    this.setupMessageType();
  }

  @Override
  public void sendMessage(String message) {
    if (messageType == null) {
      logger.warning("Expected a message-type to be set up at the time of calling EssentialsDiscordApi#sendMessage!");
      return;
    }

    this.discordService.sendMessage(
      messageType,
      message,
      config.rootSection.discord.essentialsDiscord.allowGroupMentions
    );
  }

  public void onConfigReload() {
    this.setupMessageType();
  }

  private void setupMessageType() {
    var key = config.rootSection.discord.essentialsDiscord.messageType;

    this.messageType = tryLocateDefaultType(key);

    if (this.messageType != null) {
      logger.info("Using a default EssentialsDiscord message-type called \"" + key + "\"!");
      return;
    }

    this.messageType = new MessageType(key);

    if (!discordService.isRegistered(key)) {
      discordService.registerMessageType(plugin, this.messageType);
      logger.info("Registered custom EssentialsDiscord message-type called \"" + key + "\"!");
    }

    else
      logger.info("Using already registered EssentialsDiscord message-type called \"" + key + "\"!");
  }

  private @Nullable MessageType tryLocateDefaultType(String key) {
    for (var defaultType : MessageType.DefaultTypes.values()) {
      if (defaultType.getKey().equalsIgnoreCase(key))
        return defaultType;
    }

    return null;
  }
}
