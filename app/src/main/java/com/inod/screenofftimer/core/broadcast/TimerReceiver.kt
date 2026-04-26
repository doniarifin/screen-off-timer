package com.inod.screenofftimer.core.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.inod.screenofftimer.data.prefs.Prefs
import com.inod.screenofftimer.service.TimerService

class TimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("RECEIVER", "onReceive: ${intent.action}")

        when (intent.action) {
            "ACTION_ADD_MINUTE" -> {
                val intent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_ADD_MINUTE
                    putExtra("time_update", 10 * 60)
                }

                ContextCompat.startForegroundService(context, intent)
            }

            "ACTION_REDUCE_MINUTE" -> {
                val intent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_REDUCE_MINUTE
                    putExtra("time_update", 10 * 60)
                }

                ContextCompat.startForegroundService(context, intent)
            }

            "NOTIF_STOP" -> {
                val intent = Intent(context, TimerService::class.java).apply {
                    action = TimerService.ACTION_STOP
                    putExtra("running", false)
                }

                context.startService(intent)
                Prefs.saveRunning(context, false)
            }
        }
    }
}