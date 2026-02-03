package me.blvckbytes.configurable_emotions.command.emotion_control.sub;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
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

    config.rootSection.playerMessages.profileFlagToggled.sendMessage(
      player,
      new InterpretationEnvironment()
        .withVariable("flag", flag.name())
        .withVariable("value", newValue)
    );
  }
}
