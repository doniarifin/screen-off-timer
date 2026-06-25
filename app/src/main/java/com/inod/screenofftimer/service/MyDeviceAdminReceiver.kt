package com.inod.screenofftimer.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.inod.screenofftimer.data.prefs.Prefs

class MyDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Prefs.saveDeviceAdmin(context, true)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Prefs.saveDeviceAdmin(context, false)
    }
}