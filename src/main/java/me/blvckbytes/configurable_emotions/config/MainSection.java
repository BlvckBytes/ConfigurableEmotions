package me.blvckbytes.configurable_emotions.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CSAlways
public class MainSection extends AConfigSection {

  public CommandsSection commands;
  public PlayerMessagesSection playerMessages;

  public Map<String, EmotionSection> emotions;

  @CSIgnore
  public List<ConfigEmotion> configuredEmotions;

  public MainSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);

    this.emotions = new HashMap<>();
    this.configuredEmotions = new ArrayList<>();
  }

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    for (var emotionEntry : emotions.entrySet())
      this.configuredEmotions.add(new ConfigEmotion(emotionEntry.getKey(), emotionEntry.getValue()));
  }
}
