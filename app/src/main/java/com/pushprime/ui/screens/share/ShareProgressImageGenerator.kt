package com.pushprime.ui.screens.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.math.min

enum class ShareTemplate(val displayName: String) {
    MINIMAL("Minimal"),
    BEFORE_AFTER("Before/After"),
    STATS_ONLY("Stats Only"),
    BADGE_STATS("Badge + Stats")
}

enum class ShareImageSize(val width: Int, val height: Int) {
    STORY(1080, 1920),
    SQUARE(1080, 1080)
}

data class ShareBadge(
    val icon: String,
    val label: String
)

data class SharePhoto(
    val bitmap: Bitmap?,
    val dateLabel: String?
)

data class ShareProgressData(
    val name: String,
    val goal: String,
    val avatarInitials: String,
    val streakDays: Int,
    val sessionsThisWeek: Int,
    val stepsToday: Int?,
    val badge: ShareBadge?,
    val beforePhoto: SharePhoto?,
    val afterPhoto: SharePhoto?
)

fun generateShareBitmap(template: ShareTemplate, data: ShareProgressData): Bitmap {
    return generateShareBitmap(template, data, ShareImageSize.STORY)
}

fun generateShareBitmap(
    template: ShareTemplate,
    data: ShareProgressData,
    size: ShareImageSize
): Bitmap {
    val width = size.width
    val height = size.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val padding = width * 0.07f
    val headerSpacing = width * 0.03f
    val statsAreaHeight = height * 0.18f
    val watermarkHeight = height * 0.05f

    // Background gradient
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            0f,
            0f,
            height.toFloat(),
            intArrayOf(0xFF0D0D0D.toInt(), 0xFF1E1E1E.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Avatar
    val avatarRadius = width * 0.08f
    val avatarCenterX = padding + avatarRadius
    val avatarCenterY = padding + avatarRadius
    val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
    }
    canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, avatarPaint)

    val initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF111111.toInt()
        textSize = avatarRadius * 0.9f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    drawCenteredText(canvas, data.avatarInitials, avatarCenterX, avatarCenterY, initialsPaint)

    // Name + goal
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = width * 0.06f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val goalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFBDBDBD.toInt()
        textSize = width * 0.035f
    }
    val nameX = avatarCenterX + avatarRadius + headerSpacing
    val nameY = avatarCenterY - avatarRadius * 0.2f
    canvas.drawText(data.name, nameX, nameY, namePaint)
    canvas.drawText(data.goal, nameX, nameY + namePaint.textSize + 12f, goalPaint)

    val contentTop = avatarCenterY + avatarRadius + (padding * 0.6f)
    val contentBottom = height - statsAreaHeight - watermarkHeight - padding

    when (template) {
        ShareTemplate.BEFORE_AFTER -> {
            drawBeforeAfterPhotos(
                canvas = canvas,
                width = width,
                contentTop = contentTop,
                contentBottom = contentBottom,
                padding = padding,
                beforePhoto = data.beforePhoto,
                afterPhoto = data.afterPhoto
            )
        }
        ShareTemplate.BADGE_STATS -> {
            drawBadgeCard(
                canvas = canvas,
                width = width,
                contentTop = contentTop,
                contentBottom = contentBottom,
                padding = padding,
                badge = data.badge
            )
        }
        ShareTemplate.MINIMAL,
        ShareTemplate.STATS_ONLY -> {
            drawAccentLine(canvas, width, contentTop, padding)
        }
    }

    drawStatsRow(
        canvas = canvas,
        width = width,
        height = height,
        padding = padding,
        statsAreaHeight = statsAreaHeight,
        streakDays = data.streakDays,
        sessionsThisWeek = data.sessionsThisWeek,
        stepsToday = data.stepsToday
    )

    // Watermark
    val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        alpha = 130
        textSize = width * 0.028f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText(
        "RAMBOOST",
        width / 2f,
        height - padding,
        watermarkPaint
    )

    return bitmap
}

private fun drawBeforeAfterPhotos(
    canvas: Canvas,
    width: Int,
    contentTop: Float,
    contentBottom: Float,
    padding: Float,
    beforePhoto: SharePhoto?,
    afterPhoto: SharePhoto?
) {
    val gap = padding * 0.6f
    val availableHeight = contentBottom - contentTop
    val photoWidth = (width - padding * 2 - gap) / 2f
    val photoHeight = min(availableHeight, photoWidth * 1.2f)
    val top = contentTop + (availableHeight - photoHeight) / 2f
    val radius = photoWidth * 0.08f

    val leftRect = RectF(padding, top, padding + photoWidth, top + photoHeight)
    val rightRect = RectF(padding + photoWidth + gap, top, padding + photoWidth * 2 + gap, top + photoHeight)

    drawRoundedImage(canvas, leftRect, radius, beforePhoto?.bitmap)
    drawRoundedImage(canvas, rightRect, radius, afterPhoto?.bitmap)

    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFCCCCCC.toInt()
        textSize = width * 0.028f
        textAlign = Paint.Align.CENTER
    }
    beforePhoto?.dateLabel?.let {
        canvas.drawText(it, leftRect.centerX(), leftRect.bottom + labelPaint.textSize + 12f, labelPaint)
    }
    afterPhoto?.dateLabel?.let {
        canvas.drawText(it, rightRect.centerX(), rightRect.bottom + labelPaint.textSize + 12f, labelPaint)
    }
}

private fun drawBadgeCard(
    canvas: Canvas,
    width: Int,
    contentTop: Float,
    contentBottom: Float,
    padding: Float,
    badge: ShareBadge?
) {
    if (badge == null) return
    val cardHeight = min(width * 0.18f, contentBottom - contentTop)
    val cardTop = contentTop + ((contentBottom - contentTop - cardHeight) / 2f)
    val rect = RectF(padding, cardTop, width - padding, cardTop + cardHeight)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF262626.toInt()
    }
    canvas.drawRoundRect(rect, cardHeight / 2f, cardHeight / 2f, paint)

    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = cardHeight * 0.5f
        textAlign = Paint.Align.CENTER
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = cardHeight * 0.32f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val iconCenterX = rect.left + cardHeight * 0.6f
    val iconCenterY = rect.centerY()
    drawCenteredText(canvas, badge.icon, iconCenterX, iconCenterY, iconPaint)
    canvas.drawText(badge.label, rect.left + cardHeight * 1.1f, rect.centerY() + labelPaint.textSize * 0.3f, labelPaint)
}

private fun drawAccentLine(canvas: Canvas, width: Int, contentTop: Float, padding: Float) {
    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333333.toInt()
        strokeWidth = width * 0.01f
    }
    canvas.drawLine(padding, contentTop, width - padding, contentTop, linePaint)
}

private fun drawStatsRow(
    canvas: Canvas,
    width: Int,
    height: Int,
    padding: Float,
    statsAreaHeight: Float,
    streakDays: Int,
    sessionsThisWeek: Int,
    stepsToday: Int?
) {
    val statsTop = height - statsAreaHeight - padding * 0.3f
    val statWidth = (width - padding * 2) / 3f

    drawStatItem(
        canvas = canvas,
        centerX = padding + statWidth / 2f,
        topY = statsTop,
        icon = "🔥",
        value = "$streakDays",
        label = "Streak"
    )
    drawStatItem(
        canvas = canvas,
        centerX = padding + statWidth * 1.5f,
        topY = statsTop,
        icon = "🏋️",
        value = "$sessionsThisWeek",
        label = "Sessions"
    )
    drawStatItem(
        canvas = canvas,
        centerX = padding + statWidth * 2.5f,
        topY = statsTop,
        icon = "👣",
        value = stepsToday?.toString() ?: "—",
        label = "Steps"
    )
}

private fun drawStatItem(
    canvas: Canvas,
    centerX: Float,
    topY: Float,
    icon: String,
    value: String,
    label: String
) {
    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }
    val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 56f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB0B0B0.toInt()
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    drawCenteredText(canvas, icon, centerX, topY + 22f, iconPaint)
    canvas.drawText(value, centerX, topY + 88f, valuePaint)
    canvas.drawText(label, centerX, topY + 130f, labelPaint)
}

private fun drawRoundedImage(canvas: Canvas, rect: RectF, radius: Float, bitmap: Bitmap?) {
    if (bitmap == null) return
    val path = Path().apply {
        addRoundRect(rect, radius, radius, Path.Direction.CW)
    }
    canvas.save()
    canvas.clipPath(path)
    val scaled = centerCrop(bitmap, rect.width().toInt(), rect.height().toInt())
    canvas.drawBitmap(scaled, rect.left, rect.top, null)
    canvas.restore()
}

private fun centerCrop(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
    val scale = maxOf(
        targetWidth / bitmap.width.toFloat(),
        targetHeight / bitmap.height.toFloat()
    )
    val scaledWidth = (bitmap.width * scale).toInt()
    val scaledHeight = (bitmap.height * scale).toInt()
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    val x = (scaledWidth - targetWidth) / 2
    val y = (scaledHeight - targetHeight) / 2
    return Bitmap.createBitmap(scaledBitmap, x, y, targetWidth, targetHeight)
}

private fun drawCenteredText(canvas: Canvas, text: String, centerX: Float, centerY: Float, paint: Paint) {
    val textBounds = android.graphics.Rect()
    paint.getTextBounds(text, 0, text.length, textBounds)
    val textHeight = textBounds.height()
    canvas.drawText(text, centerX, centerY + textHeight / 2f, paint)
}
