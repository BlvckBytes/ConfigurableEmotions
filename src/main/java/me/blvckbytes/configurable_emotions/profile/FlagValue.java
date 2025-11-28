package me.blvckbytes.configurable_emotions.profile;

public enum FlagValue {
  ONLY_SELF,
  SELF_AND_OTHERS,
  HIDE_ALL
  ;

  private static final FlagValue[] values = values();

  public FlagValue next() {
    var nextOrdinal = ordinal() + 1;

    if (nextOrdinal >= values.length)
      nextOrdinal = 0;

    return values[nextOrdinal];
  }

  public boolean doesShow(boolean isTargetedByEmotion) {
    return this == FlagValue.SELF_AND_OTHERS || this == FlagValue.ONLY_SELF && isTargetedByEmotion;
  }
}
