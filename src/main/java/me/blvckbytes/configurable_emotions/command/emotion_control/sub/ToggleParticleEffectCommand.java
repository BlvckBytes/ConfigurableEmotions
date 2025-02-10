package me.blvckbytes.configurable_emotions.command.emotion_control.sub;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.command.CommandFailure;
import me.blvckbytes.configurable_emotions.command.emotion_control.ControlAction;
import me.blvckbytes.configurable_emotions.config.MainSection;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileFlag;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileStore;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToggleParticleEffectCommand extends FlagToggleSubCommand {

  public ToggleParticleEffectCommand(ConfigKeeper<MainSection> config, PlayerProfileStore profileStore) {
    super(PlayerProfileFlag.PARTICLE_EFFECT_ENABLED, profileStore, config);
  }

  @Override
  public @Nullable CommandFailure onCommand(CommandSender sender, String[] args) {
    if (args.length != 0)
      return CommandFailure.INVALID_USAGE;

    if (!(sender instanceof Player player))
      return CommandFailure.PLAYER_ONLY;

    toggleAndSendMessage(player);
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
    return ControlAction.matcher.getNormalizedConstant(ControlAction.TOGGLE_PARTICLE_EFFECT);
  }
}
