package dev.slne.surf.cloud.core.common.sound

import dev.slne.surf.cloud.api.common.util.objectListOf
import dev.slne.surf.surfapi.core.api.generated.SoundKeys
import dev.slne.surf.surfapi.core.api.messages.adventure.Sound
import net.kyori.adventure.sound.Sound.Source

object CommonSounds {
    val BROADCAST_SOUND_1 = Sound {
        type(SoundKeys.BLOCK_NOTE_BLOCK_CHIME)
        pitch(0.9f)
        volume(1f)
        source(Source.MASTER)
    }
    val BROADCAST_SOUND_2 = Sound {
        type(SoundKeys.BLOCK_NOTE_BLOCK_CHIME)
        pitch(1.3f)
        volume(1f)
        source(Source.MASTER)
    }
    val broadcastSounds = objectListOf(BROADCAST_SOUND_1, BROADCAST_SOUND_2)
}