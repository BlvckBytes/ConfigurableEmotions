package me.blvckbytes.configurable_emotions.profile;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import com.google.common.base.Charsets;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import me.blvckbytes.configurable_emotions.config.MainSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerProfileStore {

  private static final Gson GSON_INSTANCE = new GsonBuilder().setPrettyPrinting().create();

  private final Map<UUID, PlayerProfile> profileByHolderId;
  private boolean isDirty;

  private final BukkitTask storeTask;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;
  private final File dataFile;

  public PlayerProfileStore(
    Plugin plugin,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.config = config;
    this.profileByHolderId = new HashMap<>();
    this.logger = logger;
    this.dataFile = new File(plugin.getDataFolder(), "player-profiles.json");

    this.load();
    this.storeTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::store, 0L, 20L * 30);
  }

  public PlayerProfile getProfile(Player player) {
    return profileByHolderId.computeIfAbsent(player.getUniqueId(), holderId -> new PlayerProfile(this, holderId));
  }

  public void onDisable() {
    this.storeTask.cancel();
    this.store();
  }

  protected FlagValue getDefaultFlagValue(PlayerProfileFlag flag) {
    return flag.accessFlagSection(config.rootSection.playerProfiles.flags).defaultValue;
  }

  @SuppressWarnings("unused")
  protected void onProfileUpdate(PlayerProfile profile) {
    this.isDirty = true;
  }

  private void load() {
    if (!dataFile.exists())
      return;

    if (!dataFile.isFile())
      throw new IllegalStateException("Expected there to be a file at path " + dataFile);

    try(
      var reader = new FileReader(dataFile, Charsets.UTF_8)
    ) {
      var jsonObject = GSON_INSTANCE.fromJson(reader, JsonObject.class);

      if (jsonObject == null)
        return;

      if (!(jsonObject.get("playerProfiles") instanceof JsonObject profilesObject))
        throw new IllegalStateException("Expected key \"playerProfiles\" to be an object");

      for (var profileEntry : profilesObject.entrySet()) {
        var profileIdString = profileEntry.getKey();

        UUID holderId;

        try {
          holderId = UUID.fromString(profileIdString);
        } catch (Exception e) {
          logger.log(Level.WARNING, "Key \"playerProfiles\".\"" + profileIdString + "\" did not represent a valid UUID; skipping!");
          continue;
        }

        var profileElement = profileEntry.getValue();

        if (!(profileElement instanceof JsonObject profileObject)) {
          logger.log(Level.WARNING, "Value at \"playerProfiles\".\"" + profileIdString + "\" is not an object; skipping!");
          continue;
        }

        var playerProfile = new PlayerProfile(this, holderId);

        for (var profileFlag : PlayerProfileFlag.values) {
          if (!(profileObject.get(profileFlag.name()) instanceof JsonPrimitive valuePrimitive))
            continue;

          var stringValue = valuePrimitive.getAsString();

          FlagValue value;

          try {
            value = FlagValue.valueOf(stringValue);
          } catch (IllegalArgumentException ignored) {
            // Automatically migrate prior schema of boolean-values
            value = switch (stringValue.toLowerCase()) {
              case "true", "yes" -> FlagValue.SELF_AND_OTHERS;
              default -> FlagValue.HIDE_ALL;
            };
          }

          playerProfile.setFlag(profileFlag, value);
        }

        profileByHolderId.put(holderId, playerProfile);
      }
    } catch (Exception e) {
      throw new IllegalStateException("An error occurred while trying to load the player-profile store!", e);
    }
  }

  private void store() {
    if (!isDirty)
      return;

    try (
      var fileWriter = new FileWriter(dataFile, Charsets.UTF_8);
      var jsonWriter = new JsonWriter(fileWriter)
    ) {
      var profileMapObject = new JsonObject();

      for (var playerProfile : profileByHolderId.values()) {
        var profileObject = new JsonObject();

        for (var profileFlag : PlayerProfileFlag.values) {
          if (playerProfile.isFlagSet(profileFlag))
            profileObject.addProperty(profileFlag.name(), playerProfile.getFlagOrDefault(profileFlag).name());
        }

        profileMapObject.add(playerProfile.holderId.toString(), profileObject);
      }

      var jsonObject = new JsonObject();
      jsonObject.add("playerProfiles", profileMapObject);

      GSON_INSTANCE.toJson(jsonObject, JsonObject.class, jsonWriter);

      this.isDirty = false;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to save the player-profile store!", e);
    }
  }
}
