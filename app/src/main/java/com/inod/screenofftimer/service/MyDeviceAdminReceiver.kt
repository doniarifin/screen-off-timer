package com.inod.screenofftimer.service

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.inod.screenofftimer.data.Prefs

class MyDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Prefs.saveDeviceAdmin(context, true)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Prefs.saveDeviceAdmin(context, false)
    }
}

fun isDpmActive(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val component = ComponentName(context, MyDeviceAdminReceiver::class.java)
    return dpm.isAdminActive(component)
}

fun requestDeviceAdmin(context: Context) {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val component = ComponentName(context, MyDeviceAdminReceiver::class.java)

    if (dpm.isAdminActive(component)) {
        Log.d("DeviceAdmin", "Device Admin already active")
        return
    }

    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
        putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Required to lock screen automatically"
        )
    }
    context.startActivity(intent)
}

fun removeDeviceAdmin(context: Context) {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val component = ComponentName(context, MyDeviceAdminReceiver::class.java)

    if (dpm.isAdminActive(component)) {
        dpm.removeActiveAdmin(component)
    }
}