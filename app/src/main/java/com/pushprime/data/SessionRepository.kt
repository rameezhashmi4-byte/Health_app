package com.pushprime.data

import com.pushprime.model.SessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class SessionRepository(
    private val sessionDao: SessionDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun insert(session: SessionEntity): Long {
        return sessionDao.insert(session)
    }

    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsForWeek(referenceDate: LocalDate = LocalDate.now()): Flow<List<SessionEntity>> {
        val start = referenceDate.with(DayOfWeek.MONDAY)
        val end = start.plusDays(6)
        return sessionDao.getSessionsByDateRange(
            start.format(dateFormatter),
            end.format(dateFormatter)
        )
    }

    fun getSessionsForMonth(referenceDate: LocalDate = LocalDate.now()): Flow<List<SessionEntity>> {
        val month = YearMonth.from(referenceDate)
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        return sessionDao.getSessionsByDateRange(
            start.format(dateFormatter),
            end.format(dateFormatter)
        )
    }

    suspend fun getUnsyncedSessions(limit: Int = 50): List<SessionEntity> {
        return sessionDao.getUnsyncedSessions(limit)
    }

    suspend fun markSynced(sessionId: Long, syncedAt: Long) {
        val session = sessionDao.getSessionById(sessionId)
        val attempts = (session?.syncAttempts ?: 0)
        sessionDao.updateSyncState(
            id = sessionId,
            isSynced = true,
            lastSyncedAt = syncedAt,
            syncAttempts = attempts
        )
    }

    suspend fun incrementSyncAttempt(sessionId: Long) {
        val session = sessionDao.getSessionById(sessionId)
        val attempts = (session?.syncAttempts ?: 0) + 1
        sessionDao.updateSyncState(
            id = sessionId,
            isSynced = false,
            lastSyncedAt = session?.lastSyncedAt,
            syncAttempts = attempts
        )
    }
}
