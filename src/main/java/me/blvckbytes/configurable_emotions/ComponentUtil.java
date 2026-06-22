package me.blvckbytes.configurable_emotions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.function.Consumer;

public class ComponentUtil {

  public static String componentToText(Component component) {
    var result = new StringBuilder();
    forEachTextOfComponent(component, result::append);
    return result.toString();
  }

  public static void forEachTextOfComponent(Component component, Consumer<String> textHandler) {
    if (component instanceof TextComponent textComponent)
      textHandler.accept(textComponent.content());

    for (var child : component.children())
      forEachTextOfComponent(child, textHandler);
  }
}
