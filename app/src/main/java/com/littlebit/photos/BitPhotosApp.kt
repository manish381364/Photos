package com.littlebit.photos

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class BitPhotosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "water_notification",
                "Water",
                NotificationManager.IMPORTANCE_LOW
            )
            val mediaChannel = NotificationChannel(
                "media_notification",
                "Media",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
            notificationManager.createNotificationChannel(mediaChannel)
        }
    }
}