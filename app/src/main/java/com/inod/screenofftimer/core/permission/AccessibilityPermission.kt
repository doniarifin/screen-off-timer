package com.inod.screenofftimer.core.permission

import android.content.Context
import android.content.Intent
import android.provider.Settings

object AccessibilityPermission {
    fun open(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}