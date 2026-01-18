package com.pushprime.data

import com.pushprime.model.SessionStatus
import com.pushprime.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

class WorkoutSessionRepository(
    private val workoutSessionDao: WorkoutSessionDao
) {

    suspend fun createSession(session: WorkoutSession): Long {
        return workoutSessionDao.insert(session)
    }

    suspend fun getSessionById(id: Long): WorkoutSession? {
        return workoutSessionDao.getById(id)
    }

    suspend fun getSessionBySessionId(sessionId: String): WorkoutSession? {
        return workoutSessionDao.getBySessionId(sessionId)
    }

    fun getActiveSessions(userId: String): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getActiveSessionsByUser(userId, SessionStatus.ACTIVE.name)
    }

    suspend fun getActiveSessionForPlan(userId: String, planId: Long): WorkoutSession? {
        return workoutSessionDao.getActiveSessionForPlan(userId, planId, SessionStatus.ACTIVE.name)
    }

    suspend fun updateSession(session: WorkoutSession) {
        workoutSessionDao.update(session)
    }

    suspend fun updateSessionStatus(sessionId: Long, status: SessionStatus) {
        workoutSessionDao.updateStatus(sessionId, status.name)
    }

    suspend fun deleteSession(id: Long) {
        workoutSessionDao.deleteById(id)
    }

    fun getAllSessionsByUser(userId: String): Flow<List<WorkoutSession>> {
        return workoutSessionDao.getAllSessionsByUser(userId)
    }
}