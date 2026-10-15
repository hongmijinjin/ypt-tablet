package com.example.ypttablet.service

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import com.example.ypttablet.model.InstalledAppInfo

object PermissionHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = "${context.packageName}/${AppBlockAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(expectedComponentName)
    }

    fun getInstalledLauncherApps(context: Context, allowedSet: Set<String>): List<InstalledAppInfo> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        return resolveInfos
            .mapNotNull { info ->
                val pkg = info.activityInfo.packageName
                if (pkg == context.packageName) return@mapNotNull null
                InstalledAppInfo(
                    appName = info.loadLabel(pm).toString(),
                    packageName = pkg,
                    icon = info.loadIcon(pm),
                    isAllowed = allowedSet.contains(pkg)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName }
    }
}
