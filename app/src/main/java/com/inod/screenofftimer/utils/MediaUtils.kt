package com.inod.screenofftimer.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.inod.screenofftimer.DEFAULT
import com.inod.screenofftimer.R
import com.inod.screenofftimer.service.MediaControlAccessibilityService

class MediaUtils () {
    fun stopMedia(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
        audioManager.dispatchMediaKeyEvent(downEvent)

        val upEvent = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }

    fun goToHome(context: Context) {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }

        // accessibility service (GLOBAL_ACTION_HOME).
        val a11y = MediaControlAccessibilityService.instance
        if (a11y != null) {
            try {
                a11y.goHomeNow()
                return
            } catch (e: Exception) {
                Log.e("goToHome", "Accessibility goHome failed, fallback", e)
            }
        }

        // startActivity directly.
        try {
            context.startActivity(homeIntent)
            return
        } catch (e: Exception) {
            Log.e("goToHome", "Direct startActivity blocked, fallback to notification", e)
        }

        // notif high-priority with PendingIntent to home.
        showGoHomeNotification(context, homeIntent)
    }

    private fun showGoHomeNotification(context: Context, homeIntent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val pi = PendingIntent.getActivity(
            context,
            99,
            homeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, DEFAULT)
            .setContentTitle("Sleep Timer")
            .setContentText("Tap to return to the home screen")
            .setSmallIcon(R.drawable.ic_stat_sleep_timer)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        NotificationManagerCompat.from(context).notify(2, notification)
    }
}
