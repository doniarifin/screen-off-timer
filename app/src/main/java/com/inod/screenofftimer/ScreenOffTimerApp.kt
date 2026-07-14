package com.inod.screenofftimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

const val DEFAULT = "timer"
const val DEFAULT_NAME = "Sleep Timer"

class ScreenOffTimerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(DEFAULT, DEFAULT_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Notification Timer"
                    setSound(null, null)
                    enableVibration(false)
                }

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)

        }
    }
}
