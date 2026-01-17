package com.pushprime.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pushprime.data.AppDatabase
import com.pushprime.data.ProgressPhotoRepository
import com.pushprime.model.SyncStatus

class ProgressPhotoSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val database = AppDatabase.getDatabase(appContext)
    private val repository = ProgressPhotoRepository(
        context = appContext,
        photoDao = database.progressPhotoDao(),
        collageDao = database.progressCollageDao()
    )

    override suspend fun doWork(): Result {
        val photos = database.progressPhotoDao().getPhotosNeedingSync(SyncStatus.SYNCED.name)
        val collages = database.progressCollageDao().getCollagesNeedingSync(SyncStatus.SYNCED.name)
        if (photos.isEmpty() && collages.isEmpty()) {
            return Result.success()
        }
        var anyFailure = false
        photos.forEach { if (!repository.uploadPhoto(it)) anyFailure = true }
        collages.forEach { if (!repository.uploadCollage(it)) anyFailure = true }
        return if (anyFailure) Result.retry() else Result.success()
    }
}
