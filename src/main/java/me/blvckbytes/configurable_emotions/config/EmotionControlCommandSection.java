package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.section.ACommandSection;
import me.blvckbytes.configurable_emotions.command.emotion_control.ControlAction;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmotionControlCommandSection extends ACommandSection {

  public static final String INITIAL_NAME = "emotionctl";

  public Map<ControlAction, BukkitEvaluable> actionNames;

  public EmotionControlCommandSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(INITIAL_NAME, baseEnvironment);

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

      normalizedConstant.setName(name.asScalar(ScalarType.STRING, builtBaseEnvironment));
    }
  }
}
