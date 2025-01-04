package me.blvckbytes.configurable_emotions.command;

public class HelpScreenEntry {

  public final String identifier;
  public final String description;
  public final boolean supports_self;
  public final boolean supports_others;
  public final boolean supports_all;

  public HelpScreenEntry(
    String identifier,
    String description,
    boolean supportsSelf,
    boolean supportsOthers,
    boolean supportsAll
  ) {
    this.identifier = identifier;
    this.description = description;
    this.supports_self = supportsSelf;
    this.supports_others = supportsOthers;
    this.supports_all = supportsAll;
  }
}
