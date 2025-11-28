package me.blvckbytes.configurable_emotions.command.emotion_control.sub;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.command.CommandFailure;
import me.blvckbytes.configurable_emotions.command.SubCommand;
import me.blvckbytes.configurable_emotions.command.emotion_control.ControlAction;
import me.blvckbytes.configurable_emotions.config.MainSection;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileFlag;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileStore;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProfileCommand implements SubCommand {

  private final ConfigKeeper<MainSection> config;
  private final PlayerProfileStore profileStore;

  public ProfileCommand(ConfigKeeper<MainSection> config, PlayerProfileStore profileStore) {
    this.config = config;
    this.profileStore = profileStore;
  }

  @Override
  public @Nullable CommandFailure onCommand(CommandSender sender, String[] args) {
    if (args.length != 0)
      return CommandFailure.INVALID_USAGE;

    if (!(sender instanceof Player player))
      return CommandFailure.PLAYER_ONLY;

    var profile = profileStore.getProfile(player);

    config.rootSection.playerMessages.playerProfileScreen.sendMessage(
      player,
      config.rootSection.getBaseEnvironment()
        .withStaticVariable("holder_name", player.getName())
        .withStaticVariable("holder_display_name", player.getDisplayName())
        .withStaticVariable("title_value", profile.getFlagOrDefault(PlayerProfileFlag.TITLE_ENABLED))
        .withStaticVariable("action_bar_value", profile.getFlagOrDefault(PlayerProfileFlag.ACTION_BAR_ENABLED))
        .withStaticVariable("chat_value", profile.getFlagOrDefault(PlayerProfileFlag.CHAT_ENABLED))
        .withStaticVariable("sound_value", profile.getFlagOrDefault(PlayerProfileFlag.SOUND_ENABLED))
        .withStaticVariable("effect_value", profile.getFlagOrDefault(PlayerProfileFlag.PARTICLE_EFFECT_ENABLED))
        .build()
    );

    return null;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, String[] args) {
    return List.of();
  }

  @Override
  public String getPartialUsage(CommandSender sender) {
    return getCorrespondingAction().getNormalizedName();
  }

  @Override
  public NormalizedConstant<ControlAction> getCorrespondingAction() {
    return ControlAction.matcher.getNormalizedConstant(ControlAction.PROFILE);
  }
}
