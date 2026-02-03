package me.blvckbytes.configurable_emotions.command.emotion_control.sub;

import at.blvckbytes.cm_mapper.ConfigKeeper;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
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

import java.util.ArrayList;
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
    var flagValues = new ArrayList<ProfileFlagValue>();

    for (var flag : PlayerProfileFlag.values())
      flagValues.add(new ProfileFlagValue(flag, profile.getFlagOrDefault(flag)));

    config.rootSection.playerMessages.playerProfileScreen.sendMessage(
      player,
      new InterpretationEnvironment()
        .withVariable("holder_name", player.getName())
        .withVariable("holder_display_name", player.displayName())
        .withVariable("flag_values", flagValues)
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
