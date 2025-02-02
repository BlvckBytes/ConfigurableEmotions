package me.blvckbytes.configurable_emotions.profile;

import me.blvckbytes.configurable_emotions.config.profle.FlagSection;
import me.blvckbytes.configurable_emotions.config.profle.FlagsSection;

import java.util.List;
import java.util.function.Function;

public enum PlayerProfileFlag {
  TITLE_ENABLED(section -> section.titleEnabled),
  ACTION_BAR_ENABLED(section -> section.actionBarEnabled),
  CHAT_ENABLED(section -> section.chatEnabled),
  SOUND_ENABLED(section -> section.soundEnabled),
  PARTICLE_EFFECT_ENABLED(section -> section.effectEnabled),
  ;

  public static final List<PlayerProfileFlag> values = List.of(values());

  private final Function<FlagsSection, FlagSection> accessor;

  PlayerProfileFlag(Function<FlagsSection, FlagSection> accessor) {
    this.accessor = accessor;
  }

  public FlagSection accessFlagSection(FlagsSection section) {
    return accessor.apply(section);
  }
}
