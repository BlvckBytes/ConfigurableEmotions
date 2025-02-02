package me.blvckbytes.configurable_emotions.command.emotion_control.sub;

import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.configurable_emotions.command.CommandFailure;
import me.blvckbytes.configurable_emotions.command.SubCommand;
import me.blvckbytes.configurable_emotions.command.emotion_control.ControlAction;
import me.blvckbytes.configurable_emotions.config.MainSection;
import me.blvckbytes.syllables_matcher.NormalizedConstant;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReloadConfigCommand extends SubCommand {

  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  public ReloadConfigCommand(
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.config = config;
    this.logger = logger;
  }

  @Override
  public @Nullable CommandFailure onCommand(CommandSender sender, String[] args, Queue<NormalizedConstant<ControlAction>> queue) {
    if (args.length != 0)
      return CommandFailure.INVALID_USAGE;

    try {
      this.config.reload();

      config.rootSection.playerMessages.actionReloadConfigSuccess.sendMessage(sender, config.rootSection.builtBaseEnvironment);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);

      config.rootSection.playerMessages.actionReloadConfigFailure.sendMessage(sender, config.rootSection.builtBaseEnvironment);
    }

    return null;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, String[] args) {
    return List.of();
  }

  @Override
  public List<String> getPartialUsages(@Nullable Queue<NormalizedConstant<ControlAction>> actions, CommandSender sender) {
    return List.of(getCorrespondingAction().getNormalizedName());
  }

  @Override
  public NormalizedConstant<ControlAction> getCorrespondingAction() {
    return ControlAction.matcher.getNormalizedConstant(ControlAction.RELOAD_CONFIG);
  }
}
