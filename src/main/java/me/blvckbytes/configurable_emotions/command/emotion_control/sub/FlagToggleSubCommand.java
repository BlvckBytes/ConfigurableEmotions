package me.blvckbytes.configurable_emotions.command.emotion_control.sub;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.command.SubCommand;
import me.blvckbytes.configurable_emotions.config.MainSection;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileFlag;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileStore;
import org.bukkit.entity.Player;

public abstract class FlagToggleSubCommand implements SubCommand {

  protected final PlayerProfileFlag flag;
  protected final PlayerProfileStore profileStore;
  protected final ConfigKeeper<MainSection> config;

  protected FlagToggleSubCommand(
    PlayerProfileFlag flag,
    PlayerProfileStore profileStore,
    ConfigKeeper<MainSection> config
  ) {
    this.flag = flag;
    this.profileStore = profileStore;
    this.config = config;
  }

  protected void toggleAndSendMessage(Player player) {
    var newValue = profileStore.getProfile(player).cycleFlagAndGet(flag);
    var flagSection = flag.accessFlagSection(config.rootSection.playerProfiles.flags);

    flagSection.toggleMessage.sendMessage(
      player,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("value", newValue)
        .build()
    );
  }
}
