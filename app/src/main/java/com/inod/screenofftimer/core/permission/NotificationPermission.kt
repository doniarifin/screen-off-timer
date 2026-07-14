package com.inod.screenofftimer.core.permission

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.inod.screenofftimer.data.Prefs

object NotificationPermission {
    fun isGranted(activity: Activity): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                NotificationManagerCompat.from(activity).areNotificationsEnabled()
            }
            else -> true
        }
    }

    fun request(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (granted == PackageManager.PERMISSION_GRANTED) return

            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )

            val hasRequestedBefore = Prefs.getHasRequestedNotifPermission(activity)

            when {
                !hasRequestedBefore -> {
                    Prefs.saveHasRequestedNotifPermission(activity, true)
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }

                shouldShowRationale -> {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }

                else -> {
                    openSettings(activity)
                }
            }
        } else {
            val enabled = NotificationManagerCompat.from(activity).areNotificationsEnabled()

            if (!enabled) {
                openSettings(activity)
            }
        }
    }

    fun openSettings(activity: Activity) {
        val intent = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                }
            }
            else -> {
                Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                    putExtra("app_package", activity.packageName)
                    putExtra("app_uid", activity.applicationInfo.uid)
                }
            }
        }

        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
            activity.startActivity(fallbackIntent)
        }
    }
}