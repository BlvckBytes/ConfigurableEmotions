package me.blvckbytes.configurable_emotions;

import me.blvckbytes.bbconfigmapper.YamlConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MessagesSectionMigrate {

  private static final String INPUT_PATH = "/Users/blvckbytes/Desktop/ConfigurableEmotions/our_config/config.yml";

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void main(String[] args) throws Exception {
    var config = new YamlConfig(null, Logger.getAnonymousLogger(), null);
    var inputFile = new File(INPUT_PATH);

    if (!inputFile.isFile())
      throw new IllegalStateException("Expected input-file at " + inputFile);

    try (
      var fileReader = new FileReader(inputFile)
    ) {
      config.load(fileReader);
    }

    if (!(config.get("emotions") instanceof Map emotions))
      throw new IllegalStateException("Expected top-level key \"emotions\" to be a mapping-node");

    for (var emotionKey : emotions.keySet()) {
      if (!(config.get("emotions." + emotionKey) instanceof Map emotion))
        throw new IllegalStateException("Expected key at \"emotions." + emotionKey + "\" to be a mapping-node");

      var targetTypes = new String[] { "Self", "One", "Many", "All" };
      var hadDelta = false;

      for (var targetType : targetTypes) {
        var messagesObject = new HashMap<String, Object>();

        Object targetObject;

        record KeyPair (String priorKey, String newKey) {}

        var keyPairs = new KeyPair[] {
          new KeyPair("messages" + targetType + "Sender", "toSender"),
          new KeyPair("messages" + targetType + "Receiver", "toReceiver"),
          new KeyPair("messages" + targetType + "Broadcast", "asBroadcast"),
          new KeyPair("message" + targetType + "Discord", "toDiscord")
        };

        for (var keyPair : keyPairs) {
          String key = keyPair.priorKey;

          if ((targetObject = emotion.get(key)) != null) {
            messagesObject.put(keyPair.newKey, targetObject);
            emotion.remove(key);
            continue;
          }

          key += "$";

          if ((targetObject = emotion.get(key)) != null) {
            messagesObject.put(keyPair.newKey + "$", targetObject);
            emotion.remove(key);
          }
        }

        if (!messagesObject.isEmpty()) {
          emotion.put("at" + targetType + "Messages", messagesObject);
          hadDelta = true;
        }
      }

      if (hadDelta)
        config.set("emotions." + emotionKey, emotion);
    }

    File inputFileBackup;
    var backupFileGeneration = 0;

    do {
      var fileName = inputFile.getName() + ".bak";

      if (++backupFileGeneration != 1)
        fileName += backupFileGeneration;

      inputFileBackup = new File(inputFile.getParentFile(), fileName);
    } while (inputFileBackup.exists());

    Files.copy(inputFile.toPath(), inputFileBackup.toPath());

    try (
      var fileWriter = new FileWriter(inputFile)
    ) {
      config.save(fileWriter);
    }
  }
}