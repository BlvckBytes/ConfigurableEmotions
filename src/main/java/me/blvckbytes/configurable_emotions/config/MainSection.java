package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bbconfigmapper.MappingError;
import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CSAlways
public class MainSection extends AConfigSection {

  public CommandsSection commands;
  public PlayerMessagesSection playerMessages;

  public Map<String, EmotionSection> emotions;

  @CSIgnore
  public Map<String, EmotionSection> emotionByIdentifierLower;

  public MainSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.emotions = new HashMap<>();
    this.emotionByIdentifierLower = new HashMap<>();
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    for (var emotionEntry : emotions.entrySet()) {
      var identifierLower = emotionEntry.getKey().toLowerCase();

      if (identifierLower.contains(" "))
        throw new MappingError("Emotion-Identifier \"" + emotionEntry.getKey() + "\" contains an illegal space!");

      this.emotionByIdentifierLower.put(identifierLower, emotionEntry.getValue());
    }
  }
}
