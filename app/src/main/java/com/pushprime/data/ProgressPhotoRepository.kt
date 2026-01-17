package com.pushprime.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pushprime.data.sync.ProgressPhotoSyncWorker
import com.pushprime.model.PoseTag
import com.pushprime.model.ProgressCollageEntity
import com.pushprime.model.ProgressPhotoEntity
import com.pushprime.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProgressPhotoRepository(
    private val context: Context,
    private val photoDao: ProgressPhotoDao,
    private val collageDao: ProgressCollageDao
) {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }
    private val storage: FirebaseStorage? = try {
        FirebaseStorage.getInstance()
    } catch (_: Exception) {
        null
    }

    fun observePhotos(uid: String) = photoDao.getPhotosForUser(uid)

    fun observePhotoById(photoId: String) = photoDao.observePhotoById(photoId)

    suspend fun getPhotoById(photoId: String) = photoDao.getPhotoById(photoId)

    fun observeCollages(uid: String) = collageDao.getCollagesForUser(uid)

    suspend fun getCollageById(collageId: String) = collageDao.getCollageById(collageId)

    suspend fun savePhotoLocally(
        uid: String,
        sourceUri: Uri,
        poseTag: PoseTag,
        notes: String?,
        takenAt: Long
    ): Result<ProgressPhotoEntity> = withContext(Dispatchers.IO) {
        try {
            val safeUid = if (uid.isNotBlank()) uid else "anonymous"
            val photoId = UUID.randomUUID().toString()
            val storagePath = "progressPhotos/$safeUid/$photoId.jpg"
            val outputDir = File(context.filesDir, "progress_photos/$safeUid")
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, "$photoId.jpg")

            val (width, height) = compressToJpeg(sourceUri, outputFile)
            val entity = ProgressPhotoEntity(
                photoId = photoId,
                uid = safeUid,
                poseTag = poseTag.name,
                notes = notes?.takeIf { it.isNotBlank() },
                takenAt = takenAt,
                createdAt = System.currentTimeMillis(),
                localPath = outputFile.absolutePath,
                storagePath = storagePath,
                width = width,
                height = height,
                syncStatus = SyncStatus.PENDING.name
            )
            photoDao.upsert(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPhoto(entity: ProgressPhotoEntity): Boolean = withContext(Dispatchers.IO) {
        val storageRef = storage ?: run {
            markPhotoFailed(entity)
            return@withContext false
        }
        val db = firestore ?: run {
            markPhotoFailed(entity)
            return@withContext false
        }
        if (entity.uid.isBlank() || entity.uid == "anonymous") {
            markPhotoFailed(entity)
            return@withContext false
        }

        val localPath = entity.localPath ?: run {
            markPhotoFailed(entity)
            return@withContext false
        }

        return@withContext try {
            val file = File(localPath)
            if (!file.exists()) {
                markPhotoFailed(entity)
                return@withContext false
            }
            val ref = storageRef.reference.child(entity.storagePath)
            ref.putFile(Uri.fromFile(file)).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            val data = hashMapOf(
                "photoId" to entity.photoId,
                "uid" to entity.uid,
                "poseTag" to entity.poseTag,
                "notes" to entity.notes,
                "takenAt" to Timestamp(Date(entity.takenAt)),
                "createdAt" to Timestamp(Date(entity.createdAt)),
                "storagePath" to entity.storagePath,
                "downloadUrl" to downloadUrl,
                "thumbUrl" to entity.thumbUrl,
                "width" to entity.width,
                "height" to entity.height
            )
            db.collection("users")
                .document(entity.uid)
                .collection("progressPhotos")
                .document(entity.photoId)
                .set(data)
                .await()
            val current = photoDao.getPhotoById(entity.photoId)
            val attempts = (current?.syncAttempts ?: 0)
            photoDao.updateSyncState(
                photoId = entity.photoId,
                status = SyncStatus.SYNCED.name,
                downloadUrl = downloadUrl,
                storagePath = entity.storagePath,
                lastSyncAt = System.currentTimeMillis(),
                syncAttempts = attempts
            )
            true
        } catch (_: Exception) {
            markPhotoFailed(entity)
            false
        }
    }

    suspend fun refreshFromRemote(uid: String) = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext
        if (uid.isBlank() || uid == "anonymous") return@withContext
        try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("progressPhotos")
                .get()
                .await()
            val localMap = photoDao.getPhotosForUserOnce(uid).associateBy { it.photoId }
            val remote = snapshot.documents.mapNotNull { doc ->
                val photoId = doc.getString("photoId") ?: doc.id
                val poseTag = doc.getString("poseTag") ?: PoseTag.FRONT.name
                val notes = doc.getString("notes")
                val takenAt = doc.getTimestamp("takenAt")?.toDate()?.time
                    ?: doc.getLong("takenAt")
                    ?: System.currentTimeMillis()
                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time
                    ?: doc.getLong("createdAt")
                    ?: takenAt
                val storagePath = doc.getString("storagePath") ?: "progressPhotos/$uid/$photoId.jpg"
                val downloadUrl = doc.getString("downloadUrl")
                val thumbUrl = doc.getString("thumbUrl")
                val width = doc.getLong("width")?.toInt()
                val height = doc.getLong("height")?.toInt()
                val localPath = localMap[photoId]?.localPath
                ProgressPhotoEntity(
                    photoId = photoId,
                    uid = uid,
                    poseTag = poseTag,
                    notes = notes,
                    takenAt = takenAt,
                    createdAt = createdAt,
                    localPath = localPath,
                    storagePath = storagePath,
                    downloadUrl = downloadUrl,
                    thumbUrl = thumbUrl,
                    width = width,
                    height = height,
                    syncStatus = SyncStatus.SYNCED.name,
                    syncAttempts = localMap[photoId]?.syncAttempts ?: 0,
                    lastSyncAt = System.currentTimeMillis()
                )
            }
            if (remote.isNotEmpty()) {
                photoDao.upsertAll(remote)
            }
        } catch (_: Exception) {
            // Best-effort refresh
        }
    }

    suspend fun deletePhoto(photo: ProgressPhotoEntity) = withContext(Dispatchers.IO) {
        try {
            photoDao.deleteById(photo.photoId)
        } catch (_: Exception) {
            // Continue cleanup even if local delete fails
        }
        photo.localPath?.let { path ->
            try {
                File(path).delete()
            } catch (_: Exception) {
                // Ignore
            }
        }
        val db = firestore
        val storageRef = storage
        if (db != null && storageRef != null && photo.uid.isNotBlank() && photo.uid != "anonymous") {
            try {
                db.collection("users")
                    .document(photo.uid)
                    .collection("progressPhotos")
                    .document(photo.photoId)
                    .delete()
                    .await()
            } catch (_: Exception) {
                // Best-effort
            }
            try {
                storageRef.reference.child(photo.storagePath).delete().await()
            } catch (_: Exception) {
                // Best-effort
            }
        }
    }

    suspend fun createCollage(
        uid: String,
        photos: List<ProgressPhotoEntity>
    ): Result<ProgressCollageEntity> = withContext(Dispatchers.IO) {
        if (photos.size < 4) {
            return@withContext Result.failure(IllegalArgumentException("Need 4 photos for collage"))
        }
        try {
            val safeUid = if (uid.isNotBlank()) uid else "anonymous"
            val collageId = UUID.randomUUID().toString()
            val storagePath = "progressCollages/$safeUid/$collageId.jpg"
            val outputDir = File(context.filesDir, "progress_photos/$safeUid/collages")
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, "$collageId.jpg")
            val collageBitmap = buildCollageBitmap(photos)
                ?: return@withContext Result.failure(IllegalStateException("Failed to build collage"))
            FileOutputStream(outputFile).use { output ->
                collageBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            }
            collageBitmap.recycle()
            val entity = ProgressCollageEntity(
                collageId = collageId,
                uid = safeUid,
                photoIds = encodePhotoIds(photos.take(4).map { it.photoId }),
                createdAt = System.currentTimeMillis(),
                localPath = outputFile.absolutePath,
                storagePath = storagePath,
                syncStatus = SyncStatus.PENDING.name
            )
            collageDao.upsert(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadCollage(entity: ProgressCollageEntity): Boolean = withContext(Dispatchers.IO) {
        val storageRef = storage ?: run {
            markCollageFailed(entity)
            return@withContext false
        }
        val db = firestore ?: run {
            markCollageFailed(entity)
            return@withContext false
        }
        if (entity.uid.isBlank() || entity.uid == "anonymous") {
            markCollageFailed(entity)
            return@withContext false
        }
        val localPath = entity.localPath ?: run {
            markCollageFailed(entity)
            return@withContext false
        }
        return@withContext try {
            val file = File(localPath)
            if (!file.exists()) {
                markCollageFailed(entity)
                return@withContext false
            }
            val ref = storageRef.reference.child(entity.storagePath)
            ref.putFile(Uri.fromFile(file)).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            val data = hashMapOf(
                "collageId" to entity.collageId,
                "photoIds" to decodePhotoIds(entity.photoIds),
                "createdAt" to Timestamp(Date(entity.createdAt)),
                "storagePath" to entity.storagePath,
                "downloadUrl" to downloadUrl
            )
            db.collection("users")
                .document(entity.uid)
                .collection("progressCollages")
                .document(entity.collageId)
                .set(data)
                .await()
            val current = collageDao.getCollageById(entity.collageId)
            val attempts = (current?.syncAttempts ?: 0)
            collageDao.updateSyncState(
                collageId = entity.collageId,
                status = SyncStatus.SYNCED.name,
                downloadUrl = downloadUrl,
                storagePath = entity.storagePath,
                lastSyncAt = System.currentTimeMillis(),
                syncAttempts = attempts
            )
            true
        } catch (_: Exception) {
            markCollageFailed(entity)
            false
        }
    }

    suspend fun retryPendingUploads(): Boolean = withContext(Dispatchers.IO) {
        val photos = photoDao.getPhotosNeedingSync(SyncStatus.SYNCED.name)
        val collages = collageDao.getCollagesNeedingSync(SyncStatus.SYNCED.name)
        var success = true
        photos.forEach { if (!uploadPhoto(it)) success = false }
        collages.forEach { if (!uploadCollage(it)) success = false }
        success
    }

    fun enqueueRetryUploads() {
        val request = OneTimeWorkRequestBuilder<ProgressPhotoSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(RETRY_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    private suspend fun markPhotoFailed(entity: ProgressPhotoEntity) {
        val current = photoDao.getPhotoById(entity.photoId)
        val attempts = (current?.syncAttempts ?: 0) + 1
        photoDao.updateSyncState(
            photoId = entity.photoId,
            status = SyncStatus.FAILED.name,
            downloadUrl = current?.downloadUrl ?: entity.downloadUrl,
            storagePath = entity.storagePath,
            lastSyncAt = current?.lastSyncAt,
            syncAttempts = attempts
        )
    }

    private suspend fun markCollageFailed(entity: ProgressCollageEntity) {
        val current = collageDao.getCollageById(entity.collageId)
        val attempts = (current?.syncAttempts ?: 0) + 1
        collageDao.updateSyncState(
            collageId = entity.collageId,
            status = SyncStatus.FAILED.name,
            downloadUrl = current?.downloadUrl ?: entity.downloadUrl,
            storagePath = entity.storagePath,
            lastSyncAt = current?.lastSyncAt,
            syncAttempts = attempts
        )
    }

    private fun compressToJpeg(sourceUri: Uri, outputFile: File): Pair<Int, Int> {
        val bitmap = decodeScaledBitmap(sourceUri, MAX_DIMENSION)
            ?: throw IllegalStateException("Unable to decode image.")
        val (width, height) = bitmap.width to bitmap.height
        FileOutputStream(outputFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }
        bitmap.recycle()
        return width to height
    }

    private fun decodeScaledBitmap(sourceUri: Uri, maxSize: Int): Bitmap? {
        val resolver = context.contentResolver
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(sourceUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }
        val sampleSize = calculateInSampleSize(boundsOptions, maxSize, maxSize)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(sourceUri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null
        val scaled = scaleBitmap(decoded, maxSize)
        if (scaled != decoded) {
            decoded.recycle()
        }
        return scaled
    }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val maxDimension = maxOf(bitmap.width, bitmap.height)
        if (maxDimension <= maxSize) return bitmap
        val scale = maxSize.toFloat() / maxDimension.toFloat()
        val targetWidth = (bitmap.width * scale).toInt()
        val targetHeight = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun buildCollageBitmap(photos: List<ProgressPhotoEntity>): Bitmap? {
        val selected = photos.take(4)
        val tileSize = COLLAGE_SIZE / 2
        val output = Bitmap.createBitmap(COLLAGE_SIZE, COLLAGE_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.BLACK)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            style = Paint.Style.FILL
        }
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#66000000")
            style = Paint.Style.FILL
        }

        selected.forEachIndexed { index, photo ->
            val path = photo.localPath ?: return@forEachIndexed
            val bitmap = decodeBitmapFromFile(path, tileSize, tileSize) ?: return@forEachIndexed
            val row = index / 2
            val col = index % 2
            val left = col * tileSize
            val top = row * tileSize
            val destRect = Rect(left, top, left + tileSize, top + tileSize)
            canvas.drawBitmap(bitmap, null, destRect, null)
            bitmap.recycle()

            val label = dateFormat.format(Date(photo.takenAt))
            val textWidth = textPaint.measureText(label)
            val padding = 12f
            val bgLeft = left + 8f
            val bgBottom = top + tileSize - 8f
            val bgTop = bgBottom - textPaint.textSize - padding
            val bgRight = bgLeft + textWidth + padding
            canvas.drawRect(bgLeft, bgTop, bgRight, bgBottom, bgPaint)
            canvas.drawText(label, bgLeft + padding / 2, bgBottom - padding / 2, textPaint)
        }

        return output
    }

    private fun decodeBitmapFromFile(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    private fun encodePhotoIds(ids: List<String>): String {
        val json = JSONArray()
        ids.forEach { json.put(it) }
        return json.toString()
    }

    private fun decodePhotoIds(raw: String): List<String> {
        return try {
            val jsonArray = JSONArray(raw)
            List(jsonArray.length()) { index -> jsonArray.getString(index) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val MAX_DIMENSION = 1080
        private const val JPEG_QUALITY = 82
        private const val COLLAGE_SIZE = 1080
        private const val RETRY_WORK_NAME = "progress_photo_retry_uploads"
    }
}
