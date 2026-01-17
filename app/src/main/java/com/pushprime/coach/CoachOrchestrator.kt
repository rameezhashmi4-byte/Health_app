package com.pushprime.coach

class CoachOrchestrator(
    private val aiProvider: AiCoachProvider,
    private val basicProvider: AiCoachProvider,
    private var voiceProvider: VoiceProvider
) {
    private var settings: CoachSettings = CoachSettings()
    private var lastPhase: SessionPhase? = null
    private var lastRoundNumber: Int = -1
    private var lastSpokenAtSeconds: Int = -9999
    private var lastFrequencySpokenAtSeconds: Int = -9999
    private var lastAiRequestAtSeconds: Int = -9999
    private var aiLinesGenerated = 0
    private var aiCache: MutableList<String> = mutableListOf()
    private var aiCacheIndex = 0
    private var recentLines: ArrayDeque<String> = ArrayDeque()
    private var sessionStarted = false
    private var lastMidwayKey: String? = null
    private var lastFinalMinuteKey: String? = null
    private var hasCompleted = false

    fun updateSettings(settings: CoachSettings) {
        this.settings = settings
        voiceProvider.setStyle(settings.style)
    }

    fun updateVoiceProvider(provider: VoiceProvider) {
        voiceProvider = provider
        voiceProvider.setStyle(settings.style)
    }

    fun resetSession() {
        lastPhase = null
        lastRoundNumber = -1
        lastSpokenAtSeconds = -9999
        lastFrequencySpokenAtSeconds = -9999
        lastAiRequestAtSeconds = -9999
        aiLinesGenerated = 0
        aiCache = mutableListOf()
        aiCacheIndex = 0
        recentLines.clear()
        sessionStarted = false
        lastMidwayKey = null
        lastFinalMinuteKey = null
        hasCompleted = false
    }

    fun stop() {
        voiceProvider.stop()
    }

    suspend fun handleSessionState(
        state: SessionState,
        userContext: UserContext,
        recentPerformance: RecentPerformance? = null
    ) {
        if (!settings.hybridEnabled) {
            lastPhase = state.phase
            lastRoundNumber = state.roundNumber
            return
        }
        if (state.isPaused) {
            lastPhase = state.phase
            lastRoundNumber = state.roundNumber
            return
        }

        val trigger = resolveTrigger(state)
        if (trigger != null) {
            val minGap = if (trigger == CoachTrigger.FREQUENCY) {
                settings.frequency.seconds
            } else {
                8
            }
            val shouldBypassGap = trigger == CoachTrigger.SESSION_START || trigger == CoachTrigger.COMPLETION
            val elapsedGap = state.secondsElapsed - lastSpokenAtSeconds
            if (elapsedGap >= minGap || shouldBypassGap) {
                val line = buildCoachLine(trigger, state, userContext, recentPerformance)
                if (line.isNotBlank()) {
                    voiceProvider.speak(line)
                    lastSpokenAtSeconds = state.secondsElapsed
                    if (trigger == CoachTrigger.FREQUENCY) {
                        lastFrequencySpokenAtSeconds = state.secondsElapsed
                    }
                    rememberLine(line)
                }
            }
        }

        lastPhase = state.phase
        lastRoundNumber = state.roundNumber
    }

    suspend fun requestManualLine(userContext: UserContext) {
        val shouldUseAi = settings.hybridEnabled && settings.intelligence == CoachIntelligence.AI_VOICE
        val contextSummary = buildContextSummary(null, userContext, null, "manual")
        val line = if (shouldUseAi) {
            val aiLine = getAiLine(contextSummary, settings.style, Int.MAX_VALUE)
            if (aiLine.isNotBlank()) aiLine else basicProvider.generateOneLiner(contextSummary, settings.style)
        } else {
            basicProvider.generateOneLiner(contextSummary, settings.style)
        }
        val safeLine = sanitizeLine(line)
        if (safeLine.isNotBlank()) {
            voiceProvider.speak(safeLine)
            rememberLine(safeLine)
        }
    }

    private fun resolveTrigger(state: SessionState): CoachTrigger? {
        if (!sessionStarted && state.secondsElapsed <= 1) {
            sessionStarted = true
            return CoachTrigger.SESSION_START
        }

        if (lastRoundNumber >= 0 && state.roundNumber != lastRoundNumber) {
            lastRoundNumber = state.roundNumber
            return CoachTrigger.ROUND_START
        }

        if (state.phase == SessionPhase.REST && lastPhase != SessionPhase.REST) {
            return CoachTrigger.REST_START
        }

        if (lastPhase == SessionPhase.REST && state.phase != SessionPhase.REST) {
            return CoachTrigger.REST_END
        }

        val midwayKey = "${state.phase}_${state.roundNumber}"
        if (state.secondsRemaining > 0 &&
            state.secondsElapsed > 0 &&
            state.secondsRemaining <= state.secondsElapsed &&
            lastMidwayKey != midwayKey
        ) {
            lastMidwayKey = midwayKey
            return CoachTrigger.MIDWAY
        }

        val finalKey = "${state.phase}_${state.roundNumber}_final"
        if (state.secondsRemaining in 1..60 && lastFinalMinuteKey != finalKey) {
            lastFinalMinuteKey = finalKey
            return CoachTrigger.FINAL_MINUTE
        }

        if (!hasCompleted && state.secondsRemaining <= 0 && state.secondsElapsed > 0) {
            hasCompleted = true
            return CoachTrigger.COMPLETION
        }

        if (state.phase == SessionPhase.MAIN &&
            state.secondsElapsed >= settings.frequency.seconds &&
            state.secondsElapsed - lastFrequencySpokenAtSeconds >= settings.frequency.seconds
        ) {
            return CoachTrigger.FREQUENCY
        }

        return null
    }

    private suspend fun buildCoachLine(
        trigger: CoachTrigger,
        state: SessionState,
        userContext: UserContext,
        recentPerformance: RecentPerformance?
    ): String {
        val contextSummary = buildContextSummary(state, userContext, recentPerformance, trigger.name)
        val shouldUseAi = settings.hybridEnabled && settings.intelligence == CoachIntelligence.AI_VOICE
        if (shouldUseAi) {
            prewarmAiCache(contextSummary, settings.style)
        }
        val aiLine = if (shouldUseAi) {
            getAiLine(contextSummary, settings.style, state.secondsElapsed)
        } else {
            ""
        }
        val rawLine = if (aiLine.isNotBlank()) {
            aiLine
        } else {
            basicProvider.generateOneLiner(contextSummary, settings.style)
        }
        val safeLine = sanitizeLine(rawLine)
        if (safeLine.isBlank()) return ""
        if (isRepeatedLine(safeLine)) {
            val fallback = sanitizeLine(basicProvider.generateOneLiner(contextSummary, settings.style))
            return if (fallback.isNotBlank() && !isRepeatedLine(fallback)) fallback else safeLine
        }
        return safeLine
    }

    private suspend fun prewarmAiCache(contextSummary: String, style: CoachStyle) {
        if (aiCache.isNotEmpty()) return
        repeat(3) {
            if (aiLinesGenerated >= MAX_AI_LINES) return
            val line = aiProvider.generateOneLiner(contextSummary, style)
            aiLinesGenerated += 1
            val safeLine = sanitizeLine(line)
            if (safeLine.isNotBlank() && aiCache.none { it.equals(safeLine, ignoreCase = true) }) {
                aiCache.add(safeLine)
            }
        }
    }

    private suspend fun getAiLine(contextSummary: String, style: CoachStyle, elapsedSeconds: Int): String {
        if (aiCache.isNotEmpty()) {
            val line = aiCache[aiCacheIndex % aiCache.size]
            aiCacheIndex += 1
            return line
        }
        if (aiLinesGenerated >= MAX_AI_LINES) return ""
        val secondsSinceLast = elapsedSeconds - lastAiRequestAtSeconds
        if (lastAiRequestAtSeconds > 0 && secondsSinceLast < AI_RATE_LIMIT_SECONDS) {
            return ""
        }
        val line = aiProvider.generateOneLiner(contextSummary, style)
        aiLinesGenerated += 1
        lastAiRequestAtSeconds = elapsedSeconds
        return line
    }

    private fun buildContextSummary(
        state: SessionState?,
        userContext: UserContext,
        recentPerformance: RecentPerformance?,
        trigger: String
    ): String {
        val parts = mutableListOf<String>()
        if (state != null) {
            parts.add("type=${state.sessionType}")
            parts.add("phase=${state.phase}")
            parts.add("round=${state.roundNumber}")
            parts.add("remain=${state.secondsRemaining}")
            parts.add("elapsed=${state.secondsElapsed}")
        }
        if (!userContext.goal.isNullOrBlank()) parts.add("goal=${userContext.goal}")
        userContext.streakDays?.let { parts.add("streak=$it") }
        userContext.stepsToday?.let { parts.add("steps=$it") }
        userContext.lastWorkoutDate?.let { parts.add("lastWorkout=$it") }
        recentPerformance?.heartRate?.let { parts.add("hr=$it") }
        recentPerformance?.reps?.let { parts.add("reps=$it") }
        recentPerformance?.effort?.let { parts.add("effort=$it") }
        parts.add("trigger=$trigger")
        return parts.joinToString(", ")
    }

    private fun rememberLine(line: String) {
        recentLines.addLast(line)
        while (recentLines.size > MAX_RECENT_LINES) {
            recentLines.removeFirst()
        }
    }

    private fun isRepeatedLine(line: String): Boolean {
        return recentLines.any { it.equals(line, ignoreCase = true) }
    }

    private enum class CoachTrigger {
        SESSION_START,
        ROUND_START,
        MIDWAY,
        FINAL_MINUTE,
        REST_START,
        REST_END,
        COMPLETION,
        FREQUENCY
    }

    private companion object {
        const val MAX_RECENT_LINES = 5
        const val MAX_AI_LINES = 6
        const val AI_RATE_LIMIT_SECONDS = 120
    }
}
