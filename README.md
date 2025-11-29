# ConfigurableEmotions

A versatile bukkit-plugin which allows its users to configure any number of custom in-game emotions, each supporting a whole host of nuanced properties.

## Permissions

- `configurableemotions.command.emotion`
  - Execute the main `/emotion` command
- `configurableemotions.command.emotionrl`
    - Execute the `/emotionrl` command to reload the configuration-file
- `configurableemotions.bypass-cooldown.<lowercase_name>`
    - Bypass the cooldown of a specific emotion (use `*` as a name to bypass all)
- `configurableemotions.bypass-hidden`
    - Bypass not being able to send emotions to hidden players (vanish, etc.)
- `configurableemotions.emotion.<lowercase_name>`
    - Enables the player to use the corresponding emotion

## Configuration

Under the top-level section called `emotions`, one may register as many custom emotions as desired, each with a unique identifier as their key, which will be used as the command-argument and thus also in auto-completion; each entry has the following structure:

```yaml
# Emotions will be displayed in this exact order on the help-screen
emotions:
  # Unique identifier; used as the command-argument and for auto-completion; cannot contain spaces
  # Permission will be of pattern configurableemotions.emotion.<lowercase_identifier>; in this case: configurableemotions.emotion.myemotion
  # As for bypassing the cooldown, the permission will be of pattern configurableemotions.bypass-cooldown.<lowercase_identifier>; in this case: configurableemotions.bypass-cooldown.myemotion
  MyEmotion:
    # When true, this emotion's identifier is tried to be registered as a direct command, e.g. /hug
    # While possibly rather convenient, this setting could pollute global namespace, so it defaults to false
    tryRegisterDirectly: true

    # When registering direct commands, aliases may be declared here
    # They will call to the same underlying emotion and require the same permission
    directAliases: []

    # If no target is specified, the emotion is to be executed on all online players
    doesNoTargetEqualsAll: false

    # Description shown on the help-screen
    description: '...'

    # Time in seconds between successive executions per player; zero means no cooldown
    cooldownSeconds: 5

    # Whether this emotion can be executed without a target, on oneself
    supportsSelf: false

    # Whether this emotion can be executed with another player as a target
    supportsOthers: false

    # How many players can this emotion be addressed at all at once? Will employ the *Many*-messages
    maximumNumberOfTargets: 3

    # Whether this emotion can be executed with all online players as a target
    supportsAll: false

    # Whether broadcast-messages as defined on this emotion will be sent to the console as well
    broadcastToConsole: false

    # The sound played at execution; delete key to play no sound
    sound: '...'

    # Pitch and volume are optional and allow for more fine-grained control
    soundPitch: 1
    soundVolume: 1

    # The particle-effects played at execution; delete section to play no effect
    effects:
      # There may be as many parallel effects as desired
      -
          displayType: 'SINGLE/CLOUD/HELIX'

          # Properties of the cloud-type
          cloudRadius: 1
          cloudParticleCount: 1

          # Properties of the helix-type
          numberOfHelixCurves: 2
          helixHeight: 2
          helixRadius: 1
          helixAngleStepSize: .1
          helixWindings: 1

          # Y-axis-offset relative to the player's feet, marking the effect's origin
          yOffset: 0
          # Time in ticks in between executions
          frequencyTicks: 15
          # Total number of executions until considered completed
          numberOfExecutions: 3
          particle: '...'
          # These options may not be applicable to all particles
          # Material of item-/block-crack, block-/falling-dust, etc.
          particleMaterial: '...'
          # Also supports RGB-notation of format 'R G B'
          particleColor: '...'
          # Size of redstone dust particles
          particleSize: 1

    # Deleting the keys of undesired messages will cause them to not be displayed

    # Played the emotion at one other player
    # - receiver_name: String
    # - receiver_display_name: String
    # - sender_name: String
    # - sender_display_name: String
    atOneMessages:
      toSender:
          chatMessage: '...'
          actionBarMessage: '...'
          titleMessage: '...'
          subTitleMessage: '...'
          # Animation-durations in ticks; only active if there is a title, a subtitle, or both
          titleFadeIn: 1
          titleStay: 1
          titleFadeOut: 1
      # Same layout as toSender
      toReceiver: '...'
      # Same layout as toSender
      asBroadcast: '...'
      # Message sent to discord
      toDiscord: '...'

    # Same layout as atOneMessages
    # Played the emotion on themselves
    # - sender_name: String
    # - sender_display_name: String
    atSelfMessages:

    # Same layout as atOneMessages
    # Played the emotion at multiple other players (not all; as determined by maximumNumberOfTargets)
    # - sender_name: String
    # - sender_display_name: String
    # - receivers_names: List<String>
    # - receivers_display_names: List<String>
    atManyMessages:

    # Same layout as atOneMessages
    # Played the emotion at all other online players
    # - receiver_name: String
    # - receiver_display_name: String
    # - sender_name: String
    # - sender_display_name: String
    atAllMessages:
```

Sounds are the names of a constant of [XSound](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XSound.java); particle are the names of a constant of [XParticle](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/particles/XParticle.java); materials are the names of a constant of [XMaterial](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XMaterial.java); colors can be the names of a constant of bukkit's [Color](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Color.html)-class.

## Ideas

- Have settable moods, which have a duration, can be changed or cleared at any time, and are put into a PAPI-variable, to be displayed in - for example - the tablist, as a suffix.
