package com.inod.screenofftimer.ui.notification

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

const val DEFAULT = "timer"
const val DEFAULT_NAME = "Sleep Timer"

class BaseNotification : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(DEFAULT, DEFAULT_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notifikasi Timer"
                    setSound(null, null)
                    enableVibration(false)
                }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)

        }
    }
}