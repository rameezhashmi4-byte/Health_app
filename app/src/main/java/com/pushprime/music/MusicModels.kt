package com.pushprime.music

import kotlin.math.max
import kotlin.math.roundToInt

enum class MusicSource(val label: String) {
    BASIC("Basic Mode (no Spotify)"),
    SPOTIFY("Connect Spotify")
}

enum class EnergyLevel(val label: String, val description: String) {
    CHILL("Chill", "steady focus"),
    FOCUS("Focus", "locked in"),
    HYPE("Hype", "high drive"),
    BEAST_MODE("Beast Mode", "max hype")
}

enum class MusicSessionType(val label: String) {
    WORKOUT("Workout"),
    SPORTS("Sports"),
    QUICK_SESSION("Quick Session")
}

enum class MusicPhase(val label: String) {
    WARM_UP("Warm-up"),
    MAIN("Main"),
    FINISHER("Finisher")
}

data class TrackInfo(
    val title: String,
    val artist: String? = null,
    val bpm: Int? = null
)

data class BpmPlaylistMapping(
    val energyStyle: EnergyLevel,
    val bpmMin: Int,
    val bpmMax: Int,
    val playlistUri: String
)

object MusicPhaseLogic {
    private const val defaultSessionSeconds = 10 * 60
    private const val warmupRatio = 0.2f
    private const val mainRatio = 0.8f
    private const val targetBpmDelta = 20
    private const val rangePadding = 10

    fun resolvePhase(
        elapsedSeconds: Int,
        totalDurationSeconds: Int,
        phaseHint: MusicPhase? = null
    ): MusicPhase {
        phaseHint?.let { return it }
        val duration = if (totalDurationSeconds > 0) totalDurationSeconds else defaultSessionSeconds
        val warmupEnd = max(1, (duration * warmupRatio).roundToInt())
        val mainEnd = max(warmupEnd + 1, (duration * mainRatio).roundToInt())
        return when {
            elapsedSeconds < warmupEnd -> MusicPhase.WARM_UP
            elapsedSeconds < mainEnd -> MusicPhase.MAIN
            else -> MusicPhase.FINISHER
        }
    }

    fun targetBpmForPhase(baseBpm: Int, phase: MusicPhase, boostBpm: Int = 0): Int {
        val adjusted = when (phase) {
            MusicPhase.WARM_UP -> baseBpm - targetBpmDelta
            MusicPhase.MAIN -> baseBpm
            MusicPhase.FINISHER -> baseBpm + targetBpmDelta
        }
        return adjusted + boostBpm
    }

    fun bpmRangeForTarget(targetBpm: Int): IntRange {
        val min = (targetBpm - rangePadding).coerceAtLeast(60)
        val max = (targetBpm + rangePadding).coerceAtMost(200)
        return min..max
    }

    fun suggestedTrackType(phase: MusicPhase): String {
        return when (phase) {
            MusicPhase.WARM_UP -> "steady focus"
            MusicPhase.MAIN -> "high drive"
            MusicPhase.FINISHER -> "max hype"
        }
    }
}
