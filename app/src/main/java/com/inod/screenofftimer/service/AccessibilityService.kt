package com.inod.screenofftimer.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import com.inod.screenofftimer.MainActivity

@SuppressLint("AccessibilityPolicy")
class MediaControlAccessibilityService : AccessibilityService() {

    companion object {
        var instance: MediaControlAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun pauseMusic() {
        performGlobalAction(GLOBAL_ACTION_MEDIA_PLAY_PAUSE)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun lockScreenNow() {
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    fun goHomeNow() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun openApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }
}