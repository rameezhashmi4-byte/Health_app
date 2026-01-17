package com.pushprime.data

import com.google.firebase.firestore.FirebaseFirestore
import com.pushprime.model.ExperienceLevel
import com.pushprime.model.FitnessGoal
import com.pushprime.model.SexOption
import com.pushprime.model.UserProfile
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    private val localStore: LocalStore,
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }
) {
    suspend fun getCachedProfile(uid: String): UserProfile? {
        return localStore.profile.value?.takeIf { it.uid == uid }
    }

    fun saveLocal(profile: UserProfile) {
        localStore.saveUserProfile(profile)
    }

    suspend fun fetchRemoteProfile(uid: String): UserProfile? {
        val db = firestore ?: return null
        return try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("profile")
                .document("profile")
                .get()
                .await()
            if (!snapshot.exists()) return null
            val name = snapshot.getString("name") ?: ""
            val goal = snapshot.getString("goal")?.let { FitnessGoal.valueOf(it) }
                ?: FitnessGoal.GET_STRONGER
            val experience = snapshot.getString("experience")?.let { ExperienceLevel.valueOf(it) }
                ?: ExperienceLevel.BEGINNER
            val weight = snapshot.getDouble("weightKg")
                ?: snapshot.getLong("weightKg")?.toDouble()
                ?: 0.0
            val height = snapshot.getDouble("heightCm")
                ?: snapshot.getLong("heightCm")?.toDouble()
                ?: 0.0
            val age = snapshot.getLong("age")?.toInt()
            val sex = snapshot.getString("sex")?.let { SexOption.valueOf(it) }
            val stepsEnabled = snapshot.getBoolean("stepTrackingEnabled") ?: false
            val createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()
            val updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
            UserProfile(
                uid = uid,
                fullName = name,
                goal = goal,
                experience = experience,
                weightKg = weight,
                heightCm = height,
                age = age,
                sex = sex,
                stepTrackingEnabled = stepsEnabled,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun updateRemoteProfile(profile: UserProfile): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            val data = mapOf(
                "name" to profile.fullName,
                "goal" to profile.goal.name,
                "experience" to profile.experience.name,
                "weightKg" to profile.weightKg,
                "heightCm" to profile.heightCm,
                "age" to profile.age,
                "sex" to profile.sex?.name,
                "stepTrackingEnabled" to profile.stepTrackingEnabled,
                "createdAt" to profile.createdAt,
                "updatedAt" to profile.updatedAt
            )
            db.collection("users")
                .document(profile.uid)
                .collection("profile")
                .document("profile")
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
package com.pushprime.data

import com.google.firebase.firestore.FirebaseFirestore
import com.pushprime.model.UserProfile
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    private val localStore: LocalStore
) {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }

    fun saveLocalProfile(profile: UserProfile) {
        localStore.saveUserProfile(profile)
    }

    suspend fun saveRemoteProfile(uid: String, profile: UserProfile): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        return try {
            val data = hashMapOf(
                "uid" to uid,
                "fullName" to profile.fullName,
                "goal" to profile.goal.name,
                "experience" to profile.experience.name,
                "weightKg" to profile.weightKg,
                "heightCm" to profile.heightCm,
                "age" to profile.age,
                "sex" to profile.sex?.name,
                "stepTrackingEnabled" to profile.stepTrackingEnabled,
                "createdAt" to profile.createdAt,
                "updatedAt" to profile.updatedAt
            )
            db.collection("users")
                .document(uid)
                .collection("profile")
                .document("summary")
                .set(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
