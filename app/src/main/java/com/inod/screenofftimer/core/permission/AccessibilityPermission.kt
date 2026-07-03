package com.inod.screenofftimer.core.permission

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.inod.screenofftimer.service.MediaControlAccessibilityService

object AccessibilityPermission {
    fun open(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun isAccessibilityEnabled(context: Context): Boolean {
        val am =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )

        return enabledServices.any {
            it.resolveInfo.serviceInfo.name.contains(
                MediaControlAccessibilityService::class.java.name
            )
        }
    }
}