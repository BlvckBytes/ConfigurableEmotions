package me.blvckbytes.configurable_emotions.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerProfile {

  private final PlayerProfileStore store;
  public final UUID holderId;

  private final Map<PlayerProfileFlag, Boolean> valueByFlag;

  protected PlayerProfile(PlayerProfileStore store, UUID holderId
  ) {
    this.store = store;
    this.holderId = holderId;
    this.valueByFlag = new HashMap<>();
  }

  public void setFlag(PlayerProfileFlag flag, boolean value) {
    this.valueByFlag.put(flag, value);
    this.store.onProfileUpdate(this);
  }

  public boolean toggleFlagAndGet(PlayerProfileFlag flag) {
    var priorValue = getFlagOrDefault(flag);
    setFlag(flag, !priorValue);
    return !priorValue;
  }

  public boolean getFlagOrDefault(PlayerProfileFlag flag) {
    return valueByFlag.computeIfAbsent(flag, store::getDefaultFlagValue);
  }

  public boolean isFlagSet(PlayerProfileFlag flag) {
    return valueByFlag.containsKey(flag);
  }
}
