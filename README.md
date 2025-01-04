# ConfigurableEmotions

A versatile bukkit-plugin which allows its users to configure any number of custom in-game emotions, each supporting a whole host of nuanced properties.

## Configuration

Under the top-level section called `emotions`, one may register as many custom emotions as desired, each with a unique identifier as their key, which will be used as the command-argument and thus also in auto-completion; each entry has the following structure:

```yaml
# Emotions will be displayed in this exact order on the help-screen
emotions:
  # Unique identifier; used as the command-argument and for auto-completion
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
      particle: '...'
      displayType: 'SINGLE/CLOUD/HELIX'
      offsetType: 'FEET/EYES/ABOVE_HEAD'

    # The particle-effect played to the receiver(s) at execution; delete section to play no effect
    effectReceiver:
      particle: '...'
      displayType: 'SINGLE/CLOUD/HELIX'
      offsetType: 'FEET/EYES/ABOVE_HEAD'

    # Deleting the keys of undesired messages will cause them to not be displayed

    # Played the emotion on themselves

    chatMessageSelfSender: '...'
    actionBarMessageSelfSender: '...'
    titleMessageSelfSender: '...'
    subTitleMessageSelfSender: '...'

    chatMessageSelfBroadcast: '...'
    actionBarMessageSelfBroadcast: '...'
    titleMessageSelfBroadcast: '...'
    subTitleMessageSelfBroadcast: '...'

    # Played the emotion at one other player

    chatMessageOneSender: '...'
    actionBarMessageOneSender: '...'
    titleMessageOneSender: '...'
    subTitleMessageOneSender: '...'

    chatMessageOneReceiver: '...'
    actionBarMessageOneReceiver: '...'
    titleMessageOneReceiver: '...'
    subTitleMessageOneReceiver: '...'

    chatMessageOneBroadcast: '...'
    actionBarMessageOneBroadcast: '...'
    titleMessageOneBroadcast: '...'
    subTitleMessageOneBroadcast: '...'

    # Played the emotion at all other online players

    chatMessageAllSender: '...'
    actionBarMessageAllSender: '...'
    titleMessageAllSender: '...'
    subTitleMessageAllSender: '...'

    chatMessageAllReceiver: '...'
    actionBarMessageAllReceiver: '...'
    titleMessageAllReceiver: '...'
    subTitleMessageAllReceiver: '...'

    chatMessageAllBroadcast: '...'
    actionBarMessageAllBroadcast: '...'
    titleMessageAllBroadcast: '...'
    subTitleMessageAllBroadcast: '...'
```

Sounds are the names of an enum-constant of [XSound](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XSound.java); particle are the names of an enum-constant of [XParticle](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/particles/XParticle.java).