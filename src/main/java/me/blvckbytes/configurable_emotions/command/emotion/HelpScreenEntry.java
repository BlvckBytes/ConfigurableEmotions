package me.blvckbytes.configurable_emotions.command.emotion;

import at.blvckbytes.component_markup.markup.interpreter.DirectFieldAccess;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class HelpScreenEntry implements DirectFieldAccess {

  public final String identifier;
  public final Component description;
  public final List<String> aliases;
  public final boolean supportsSelf;
  public final boolean supportsOthers;
  public final boolean supportsAll;

  public HelpScreenEntry(
    String identifier,
    Component description,
    List<String> aliases,
    boolean supportsSelf,
    boolean supportsOthers,
    boolean supportsAll
  ) {
    this.identifier = identifier;
    this.description = description;
    this.aliases = aliases;
    this.supportsSelf = supportsSelf;
    this.supportsOthers = supportsOthers;
    this.supportsAll = supportsAll;
  }

  @Override
  public @Nullable Object accessField(String rawIdentifier) {
    return switch (rawIdentifier) {
      case "identifier" -> identifier;
      case "description" -> description;
      case "aliases" -> aliases;
      case "supports_self" -> supportsSelf;
      case "supports_others" -> supportsOthers;
      case "supports_all" -> supportsAll;
      default -> DirectFieldAccess.UNKNOWN_FIELD_SENTINEL;
    };
  }

  @Override
  public @Nullable Set<String> getAvailableFields() {
    return Set.of("identifier", "description", "aliases", "supports_self", "supports_others", "supports_all");
  }
}
