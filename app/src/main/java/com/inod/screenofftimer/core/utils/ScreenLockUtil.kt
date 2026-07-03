package com.inod.screenofftimer.core.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.inod.screenofftimer.service.MediaControlAccessibilityService
import com.inod.screenofftimer.service.MyDeviceAdminReceiver

fun lockScreenAdmin(context: Context) {
    val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    val componentName = ComponentName(context, MyDeviceAdminReceiver::class.java)

    if (devicePolicyManager.isAdminActive(componentName)) {
        devicePolicyManager.lockNow()
    } else {
        //user ask to activate admin
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        context.startActivity(intent)
    }
}

fun lockScreenDevice(context: Context): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE)
            as DevicePolicyManager
    val component = ComponentName(context, MyDeviceAdminReceiver::class.java)

    val isAdminActive = dpm.isAdminActive(component)
    Log.d("lockScreen", "isAdminActive: $isAdminActive")

    if (isAdminActive) {
        return try {
            dpm.lockNow()
            Log.d("lockScreen", "lockNow success via Device Admin")
            true
        } catch (e: SecurityException) {
            Log.e("lockScreen", "error: ", e)
            false
        }
    }

    val accessibilityInstance = MediaControlAccessibilityService.instance
    if (accessibilityInstance != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val success = accessibilityInstance.lockScreenNow()
        Log.d("lockScreen", "lockScreen via Accessibility result: $success")
        return true
    }

    Log.w("lockScreen", "No lock method available - admin: $isAdminActive, accessibility: ${accessibilityInstance != null}")
    return false
}