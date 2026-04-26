package com.inod.screenofftimer.core.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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