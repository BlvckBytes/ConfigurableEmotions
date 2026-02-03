package me.blvckbytes.configurable_emotions.command.emotion_control.sub;

import at.blvckbytes.component_markup.markup.interpreter.DirectFieldAccess;
import me.blvckbytes.configurable_emotions.profile.FlagValue;
import me.blvckbytes.configurable_emotions.profile.PlayerProfileFlag;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ProfileFlagValue implements DirectFieldAccess {

  private final PlayerProfileFlag flag;
  private final FlagValue value;

  public ProfileFlagValue(PlayerProfileFlag flag, FlagValue value) {
    this.flag = flag;
    this.value = value;
  }

  @Override
  public @Nullable Object accessField(String rawIdentifier) {
    return switch (rawIdentifier) {
      case "flag" -> flag.name();
      case "value" -> value.name();
      default -> DirectFieldAccess.UNKNOWN_FIELD_SENTINEL;
    };
  }

  @Override
  public @Nullable Set<String> getAvailableFields() {
    return Set.of("flag", "value");
  }
}
