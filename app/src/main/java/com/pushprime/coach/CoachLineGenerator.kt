package com.pushprime.coach

private val bannedTokens = listOf(
    "doctor",
    "diagnose",
    "medical",
    "injury",
    "pain",
    "sick",
    "kill",
    "die"
)

suspend fun generateCoachLine(request: CoachLineRequest): String {
    val provider = if (request.useAi) request.aiProvider else request.fallbackProvider
    val rawLine = provider.generateOneLiner(request.contextSummary, request.style)
    val safeLine = sanitizeLine(rawLine)
    if (safeLine.isBlank() || containsBannedContent(safeLine)) {
        val fallback = request.fallbackProvider.generateOneLiner(request.contextSummary, request.style)
        return sanitizeLine(fallback)
    }
    return safeLine
}

fun sanitizeLine(line: String): String {
    val normalized = line.replace(Regex("\\s+"), " ").trim()
    if (normalized.isBlank()) return ""
    val words = normalized.split(" ")
    return if (words.size > 12) {
        words.take(12).joinToString(" ")
    } else {
        normalized
    }
}

private fun containsBannedContent(line: String): Boolean {
    val lower = line.lowercase()
    return bannedTokens.any { token -> token in lower }
}
