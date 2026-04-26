package com.inod.screenofftimer.core.notification

import android.Manifest.permission.POST_NOTIFICATIONS
//noinspection SuspiciousImport
//import android.R
import com.inod.screenofftimer.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.inod.screenofftimer.MainActivity
import com.inod.screenofftimer.core.broadcast.TimerReceiver
import com.inod.screenofftimer.ui.notification.DEFAULT

class Notifications() {

    @SuppressLint("DefaultLocale")
    fun defaultNotification(context: Context, time: Int): Notification {

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //add min
        val addMinuteIntent = Intent(context, TimerReceiver::class.java).apply {
            action = "ACTION_ADD_MINUTE"
        }

        val addMinutePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            addMinuteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //reduce min
        val reduceMinutesIntent = Intent(context, TimerReceiver::class.java).apply {
            action = "ACTION_REDUCE_MINUTE"
        }

        val reduceMinutesPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            reduceMinutesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //Stop
        val stopIntent = Intent(context, TimerReceiver::class.java).apply {
            action = "NOTIF_STOP"
            putExtra("time", time)
        }

        val stopIntentPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, DEFAULT)
            .setContentTitle("Sleep Timer")
            .setOnlyAlertOnce(true)
            .setContentText("Sleep in ${String.format("%02d:%02d", time / 60, time % 60)}")
            .setSmallIcon(R.drawable.ic_stat_sleep_timer)
            .setSound(null)
            .setVibrate(longArrayOf(0))
            .setShowWhen(false)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, "STOP", stopIntentPendingIntent)
            .addAction(0, "- 10 Min", reduceMinutesPendingIntent)
            .addAction(0, "+ 10 Min", addMinutePendingIntent)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        return notification
    }

    fun defaultNotificationCompat(context: Context, time: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    POST_NOTIFICATIONS

                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context)
                    .notify(1, defaultNotification(context, time))
            }
        } else {
            NotificationManagerCompat.from(context).notify(1, defaultNotification(context, time))
        }
    }
}