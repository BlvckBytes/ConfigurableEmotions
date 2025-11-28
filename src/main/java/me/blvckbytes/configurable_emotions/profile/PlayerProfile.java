package me.blvckbytes.configurable_emotions.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerProfile {

  private final PlayerProfileStore store;
  public final UUID holderId;

  private final Map<PlayerProfileFlag, FlagValue> valueByFlag;

  protected PlayerProfile(PlayerProfileStore store, UUID holderId
  ) {
    this.store = store;
    this.holderId = holderId;
    this.valueByFlag = new HashMap<>();
  }

  public void setFlag(PlayerProfileFlag flag, FlagValue value) {
    this.valueByFlag.put(flag, value);
    this.store.onProfileUpdate(this);
  }

  public FlagValue cycleFlagAndGet(PlayerProfileFlag flag) {
    var priorValue = getFlagOrDefault(flag);
    var nextValue = priorValue.next();
    setFlag(flag, nextValue);
    return nextValue;
  }

  public FlagValue getFlagOrDefault(PlayerProfileFlag flag) {
    return valueByFlag.computeIfAbsent(flag, store::getDefaultFlagValue);
  }

  public boolean isFlagSet(PlayerProfileFlag flag) {
    return valueByFlag.containsKey(flag);
  }
}
