package com.littlebit.photos.notification

import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.littlebit.photos.R
import kotlin.random.Random

class NotificationService(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val mediaNotification = NotificationCompat.Builder(context, "media_notification")
    private val mediaId = Random.nextInt()
    fun showBasicNotification() {
        val notification = NotificationCompat.Builder(context, "water_notification")
            .setContentTitle("Water Reminder")
            .setContentText("Time to drink a glass of water")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            Random.nextInt(),
            notification
        )
    }

    fun showExpandableNotification() {
        val notification = NotificationCompat.Builder(context, "water_notification")
            .setContentTitle("Water Reminder")
            .setContentText("Time to drink a glass of water")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat
                    .BigPictureStyle()
                    .bigLargeIcon(
                        context.bitmapFromResource(
                            R.drawable.baseline_notifications_24
                        )
                    )
            )
            .build()
        notificationManager.notify(Random.nextInt(), notification)
    }

    fun showMediaProgress(
        progress: Int,
        duration: Int,
        title: String,
        Artist: String?,
    ) {
        mediaNotification .setContentTitle(title)
            .setContentText(Artist ?: "Unknown Artist")
            .setSmallIcon(R.drawable.baseline_music_note_24)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat
                    .BigPictureStyle()
                    .bigLargeIcon(
                        context.bitmapFromResource(
                            R.drawable.baseline_music_note_24
                        )
                    )
            )
            .setProgress(duration, progress, false)
        notificationManager.notify(mediaId, mediaNotification.build())
    }

    private fun Context.bitmapFromResource(
        @DrawableRes resId: Int
    ) = BitmapFactory.decodeResource(
        resources,
        resId
    )
}