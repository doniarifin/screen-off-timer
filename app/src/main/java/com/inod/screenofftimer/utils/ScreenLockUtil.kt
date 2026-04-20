package com.inod.screenofftimer.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.inod.screenofftimer.MyDeviceAdminReceiver

fun lockScreen(context: Context) {
    val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    val componentName = ComponentName(context, MyDeviceAdminReceiver::class.java)

    if (devicePolicyManager.isAdminActive(componentName)) {
        devicePolicyManager.lockNow()
    } else {
        // Minta user aktifin admin
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        context.startActivity(intent)
    }
}