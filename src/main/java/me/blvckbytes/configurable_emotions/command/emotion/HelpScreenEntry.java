package me.blvckbytes.configurable_emotions.command.emotion;

import java.util.List;

public class HelpScreenEntry {

  public final String identifier;
  public final String description;
  public final List<String> aliases;
  public final boolean supports_self;
  public final boolean supports_others;
  public final boolean supports_all;

  public HelpScreenEntry(
    String identifier,
    String description,
    List<String> aliases,
    boolean supportsSelf,
    boolean supportsOthers,
    boolean supportsAll
  ) {
    this.identifier = identifier;
    this.description = description;
    this.aliases = aliases;
    this.supports_self = supportsSelf;
    this.supports_others = supportsOthers;
    this.supports_all = supportsAll;
  }
}
