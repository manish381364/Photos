package com.littlebit.photos

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

class BitPhotosApp : Application() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
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
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        notificationManager.createNotificationChannel(mediaChannel)
    }
}