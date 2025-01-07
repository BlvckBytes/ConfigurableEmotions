# ConfigurableEmotions

A versatile bukkit-plugin which allows its users to configure any number of custom in-game emotions, each supporting a whole host of nuanced properties.

## Configuration

Under the top-level section called `emotions`, one may register as many custom emotions as desired, each with a unique identifier as their key, which will be used as the command-argument and thus also in auto-completion; each entry has the following structure:

```yaml
# Emotions will be displayed in this exact order on the help-screen
emotions:
  # Unique identifier; used as the command-argument and for auto-completion; cannot contain spaces
  # Permission will be of pattern configurableemotions.emotion.<lowercase_identifier>; in this case: configurableemotions.emotion.myemotion
  MyEmotion:
    # Description shown on the help-screen
    description: '...'

    # Time in seconds between successive executions per player; zero means no cooldown
    cooldownSeconds: 5

    # Whether this emotion can be executed without a target, on oneself
    supportsSelf: false

    # Whether this emotion can be executed with another player as a target
    supportsOthers: false

    # Whether this emotion can be executed with all online players as a target
    supportsAll: false

    # The sound played to the executor at execution; delete key to play no sound
    soundSender: '...'

    # The sound played to the receiver(s) at execution; delete key to play no sound
    soundReceiver: '...'

    # The particle-effect played to the executor at execution; delete section to play no effect
    effectSender:
      displayType: 'SINGLE/CLOUD/HELIX'
      offsetType: 'FEET/EYES/ABOVE_HEAD'
      # Time in ticks in between executions
      frequencyTicks: 15
      # Total number of executions until considered completed
      numberOfExecutions: 15
      particle: '...'
      # These options may not be applicable to all particles
      # Material of item-/block-crack, block-/falling-dust, etc.
      particleMaterial: '...'
      # Also supports RGB-notation of format 'R G B'
      particleColor: '...'
      # Size of redstone dust particles
      particleSize: 1

    # The particle-effect played to the receiver(s) at execution; delete section to play no effect
    effectReceiver:
      displayType: 'SINGLE/CLOUD/HELIX'
      offsetType: 'FEET/EYES/ABOVE_HEAD'
      particle: '...'
      # These options may not be applicable to all particles
      # Material of item-/block-crack, block-/falling-dust, etc.
      particleMaterial: '...'
      # Also supports RGB-notation of format 'R G B'
      particleColor: '...'
      # Size of redstone dust particles
      particleSize: 1

    # Deleting the keys of undesired messages will cause them to not be displayed

    # Played the emotion on themselves

    messagesSelfSender:
      chatMessage: '...'
      actionBarMessage: '...'
      titleMessage: '...'
      subTitleMessage: '...'
      # Animation-durations in ticks; only active if there is a title, a subtitle, or both
      titleFadeIn: 1
      titleStay: 1
      titleFadeOut: 1

    messagesSelfBroadcast:
      chatMessage: '...'
      actionBarMessage: '...'
      titleMessage: '...'
      subTitleMessage: '...'
      # Animation-durations in ticks; only active if there is a title, a subtitle, or both
      titleFadeIn: 1
      titleStay: 1
      titleFadeOut: 1

    # Played the emotion at one other player

    messagesOneSender:
      chatMessage: '...'
      actionBarMessage: '...'
      titleMessage: '...'
      subTitleMessage: '...'
      # Animation-durations in ticks; only active if there is a title, a subtitle, or both
      titleFadeIn: 1
      titleStay: 1
      titleFadeOut: 1

    messagesOneReceiver:
      chatMessage: '...'
      actionBarMessage: '...'
      titleMessage: '...'
      subTitleMessage: '...'
      # Animation-durations in ticks; only active if there is a title, a subtitle, or both
      titleFadeIn: 1
      titleStay: 1
      titleFadeOut: 1

    messagesOneBroadcast:
      chatMessage: '...'
      actionBarMessage: '...'
      titleMessage: '...'
      subTitleMessage: '...'
      # Animation-durations in ticks; only active if there is a title, a subtitle, or both
      titleFadeIn: 1
      titleStay: 1
      titleFadeOut: 1

    # Played the emotion at all other online players

    messagesAllSender:
      chatMessage: '...'
      actionBarMessage: '...'
      titleMessage: '...'
      subTitleMessage: '...'
      # Animation-durations in ticks; only active if there is a title, a subtitle, or both
      titleFadeIn: 1
      titleStay: 1
      titleFadeOut: 1

    messagesAllReceiver:
      chatMessage: '...'
      actionBarMessage: '...'
      titleMessage: '...'
      subTitleMessage: '...'
      # Animation-durations in ticks; only active if there is a title, a subtitle, or both
      titleFadeIn: 1
      titleStay: 1
      titleFadeOut: 1

    messagesAllBroadcast:
      chatMessage: '...'
      actionBarMessage: '...'
      titleMessage: '...'
      subTitleMessage: '...'
      # Animation-durations in ticks; only active if there is a title, a subtitle, or both
      titleFadeIn: 1
      titleStay: 1
      titleFadeOut: 1
```

Sounds are the names of a constant of [XSound](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XSound.java); particle are the names of a constant of [XParticle](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/particles/XParticle.java); materials are the names of a constant of [XMaterial](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XMaterial.java); colors can be the names of a constant of bukkit's [Color](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Color.html)-class.

## Ideas

- Have settable moods, which have a duration, can be changed or cleared at any time, and are put into a PAPI-variable, to be displayed in - for example - the tablist, as a suffix.