package me.blvckbytes.configurable_emotions.config;

import at.blvckbytes.cm_mapper.cm.ComponentMarkup;
import at.blvckbytes.cm_mapper.section.command.CommandSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import me.blvckbytes.configurable_emotions.command.emotion_control.ControlAction;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmotionControlCommandSection extends CommandSection {

  public static final String INITIAL_NAME = "emotionctl";

  public Map<ControlAction, ComponentMarkup> actionNames;

  public EmotionControlCommandSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(INITIAL_NAME, baseEnvironment, interpreterLogger);

    this.actionNames = new HashMap<>();
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    for (var action : ControlAction.values()) {
      var normalizedConstant = ControlAction.matcher.getNormalizedConstant(action);
      var name = actionNames.get(action);

      if (name == null) {
        normalizedConstant.setName(normalizedConstant.initialNormalizedName);
        continue;
      }

      normalizedConstant.setName(name.asPlainString(null));
    }
  }
}
