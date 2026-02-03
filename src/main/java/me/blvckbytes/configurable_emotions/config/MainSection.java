package me.blvckbytes.configurable_emotions.config;

import at.blvckbytes.cm_mapper.mapper.MappingError;
import at.blvckbytes.cm_mapper.mapper.section.CSAlways;
import at.blvckbytes.cm_mapper.mapper.section.CSIgnore;
import at.blvckbytes.cm_mapper.mapper.section.ConfigSection;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import me.blvckbytes.configurable_emotions.config.profle.PlayerProfilesSection;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CSAlways
public class MainSection extends ConfigSection {

  public CommandsSection commands;
  public PlayerMessagesSection playerMessages;
  public DiscordSection discord;
  public PlayerProfilesSection playerProfiles;

  public Map<String, EmotionSection> emotions;

  @CSIgnore
  public Map<String, EmotionSection> emotionByIdentifierLower;

  public MainSection(InterpretationEnvironment baseEnvironment, InterpreterLogger interpreterLogger) {
    super(baseEnvironment, interpreterLogger);

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
