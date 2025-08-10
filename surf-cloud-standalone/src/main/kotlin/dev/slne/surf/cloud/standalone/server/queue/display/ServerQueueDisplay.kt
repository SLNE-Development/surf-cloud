package dev.slne.surf.cloud.standalone.server.queue.display

import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder
import dev.slne.surf.cloud.standalone.server.queue.GroupQueueImpl
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.util.mutableLong2IntMapOf
import net.kyori.adventure.text.format.TextDecoration

class ServerQueueDisplay(private val queue: GroupQueueImpl, private val queues: QueueRepository) {
    private var animationIndex = 0

    suspend fun tickDisplay() {
        val frame = ANIMATION_CHARS[animationIndex]
        animationIndex = (animationIndex + 1) % ANIMATION_CHARS.size

        val snapshot = queue.entriesSnapshot()
        if (snapshot.isEmpty()) return

        var unspecifiedCount = 0                                           // preferred == null
        val preferredCounts = mutableLong2IntMapOf() // uid -> count

        snapshot.forEach { e ->
            val key = e.preferredServerUid
            if (key == null) unspecifiedCount++
            else preferredCounts.mergeInt(key, 1, Int::plus)
        }

        var unspecifiedIndex = 0
        val preferredIndices = mutableLong2IntMapOf() // uid -> next index

        snapshot.forEach { entry ->
            val preferredUid = entry.preferredServerUid

            val pos: Int
            val max: Int
            val queueName: String
            val suspended: Boolean

            if (preferredUid == null) { // Player without a preferred server
                pos = unspecifiedIndex++
                max = unspecifiedCount
                queueName = queue.getQueueName()
                suspended = queue.suspended
            } else { // Player with a preferred server
                val suffixIndex = preferredIndices.getOrDefault(preferredUid, 0)
                pos = unspecifiedIndex + suffixIndex
                preferredIndices[preferredUid] = suffixIndex + 1

                val serverQueue = queues.getServer(preferredUid)
                max = unspecifiedCount + preferredCounts.get(preferredUid)
                queueName = serverQueue.getQueueName()
                suspended = serverQueue.suspended
            }

            val actionBar = buildActionBar(frame, pos, max, queueName, suspended)
            entry.handle.uuid.toCloudPlayer()?.sendActionBar(actionBar)
        }
    }

    private fun buildActionBar(
        ch: Char,
        pos: Int,
        max: Int,
        queueName: String,
        suspended: Boolean
    ) = buildText {
        val suspendedQueueCharacter =
            bean<StandaloneConfigHolder>().config.queue.suspendedQueueCharacter
        append {
            if (suspended) {
                error(suspendedQueueCharacter)
            } else {
                text(ch, Colors.WHITE)
            }
            decorate(TextDecoration.BOLD)
        }
        info(" Warteschlange für: ")
        variableValue(queueName)
        spacer(" | ")
        info("Position ")
        variableValue(pos + 1)
        info(" von ")
        variableValue(max)
        appendSpace()
        append {
            if (suspended) {
                error(suspendedQueueCharacter)
            } else {
                text(ch, Colors.WHITE)
            }
            decorate(TextDecoration.BOLD)
        }
    }

    companion object {
        val ANIMATION_CHARS = charArrayOf(
            '◴', '◷', '◶', '◵' //      '▁', // ▁ Lower one eighth block
            //      '▂', // ▂ Lower one quarter block
            //      '▃', // ▃ Lower three eighths block
            //      '▄', // ▄ Lower half block
            //      '▅', // ▅ Lower five eighths block
            //      '▆', // ▆ Lower three quarters block
            //      '▇', // ▇ Lower seven eighths block*
            //      '█', // █ Full block
            //      '▇', // ▇ Lower seven eighths block
            //      '▆', // ▆ Lower three quarters block
            //      '▅', // ▅ Lower five eighths block
            //      '▄', // ▄ Lower half block
            //      '▃', // ▃ Lower three eighths block
            //      '▂', // ▂ Lower one quarter block
            //      '▁'  // ▁ Lower one eighth block
            //      '⠁', '⠃', '⠇', '⠧', '⠷', '⠿', '⡿', '⢿',
            //      '⣟', '⣯', '⣷', '⣾', '⣿'
            //      , '⣾', '⣷', '⣯', '⣟', '⢿', '⡿', '⠿', '⠷', '⠧', '⠇', '⠃'
        )
    }
}