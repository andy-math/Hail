package com.aistra.hail.utils

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import com.aistra.hail.HailApp
import com.aistra.hail.receiver.DeviceAdminReceiver

object HPolicy {
    const val ADB_SET_DO =
        "adb shell dpm set-device-owner com.aistra.hail/.receiver.DeviceAdminReceiver"
    private val dpm = HailApp.app.getSystemService<DevicePolicyManager>()!!
    private val admin = ComponentName(HailApp.app, DeviceAdminReceiver::class.java)

    private val isDeviceOwner get() = dpm.isDeviceOwnerApp(HailApp.app.packageName)
    val isDeviceOwnerActive get() = isDeviceOwner && dpm.isAdminActive(admin)

    fun isAppHidden(packageName: String): Boolean =
        isDeviceOwnerActive && dpm.isApplicationHidden(admin, packageName)

    fun setAppHidden(packageName: String, hidden: Boolean): Boolean =
        isDeviceOwnerActive && dpm.setApplicationHidden(admin, packageName, hidden)

    fun uninstallApp(packageName: String): Boolean = when {
        isDeviceOwnerActive -> {
            HailApp.app.packageManager.packageInstaller.uninstall(
                packageName, PendingIntent.getActivity(
                    HailApp.app, 0, Intent(), PendingIntent.FLAG_IMMUTABLE
                ).intentSender
            )
            true
        }
        else -> false
    }

    fun enableBackupService() {
        if (isDeviceOwnerActive
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && !dpm.isBackupServiceEnabled(admin)
        ) {
            dpm.setBackupServiceEnabled(admin, true)
            HLog.i("isBackupServiceEnabled: ${dpm.isBackupServiceEnabled(admin)}")
        }
    }

    fun setOrganizationName(name: String? = null) {
        if (isDeviceOwnerActive
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) dpm.setOrganizationName(admin, name)
    }

    fun clearDeviceOwnerApp() {
        if (isDeviceOwnerActive) dpm.clearDeviceOwnerApp(HailApp.app.packageName)
    }
}