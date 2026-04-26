package com.inod.screenofftimer.core.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import com.inod.screenofftimer.service.MediaControlAccessibilityService

class AccessibilityUtils {
    fun isAccessibilityEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

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