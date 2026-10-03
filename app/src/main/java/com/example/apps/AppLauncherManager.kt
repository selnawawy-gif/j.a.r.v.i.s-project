package com.example.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

class AppLauncherManager(private val context: Context) {

    fun getInstalledApps(): List<InstalledAppItem> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val appList = mutableListOf<InstalledAppItem>()

        for (info in resolveInfos) {
            val appName = info.loadLabel(pm).toString()
            val pkg = info.activityInfo.packageName
            // Exclude Jarvis itself from app launcher shortcuts
            if (pkg != context.packageName) {
                val isSys = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                appList.add(InstalledAppItem(name = appName, packageName = pkg, isSystemApp = isSys))
            }
        }

        return appList.sortedBy { it.name.lowercase() }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun findAndLaunchApp(query: String): Pair<Boolean, String?> {
        val cleanQuery = query.lowercase()
            .replace("open ", "")
            .replace("launch ", "")
            .replace("start ", "")
            .replace("please ", "")
            .replace("jarvis ", "")
            .replace("hey ", "")
            .replace("the ", "")
            .replace("app", "")
            .trim()

        if (cleanQuery.isEmpty()) {
            return Pair(false, null)
        }

        // Handle common shortcuts
        if (cleanQuery.contains("settings")) {
            return try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Pair(true, "Settings")
            } catch (e: Exception) {
                Pair(false, null)
            }
        }

        val apps = getInstalledApps()

        // 1. Exact match
        val exactMatch = apps.find { it.name.equals(cleanQuery, ignoreCase = true) }
        if (exactMatch != null) {
            val launched = launchApp(exactMatch.packageName)
            return Pair(launched, exactMatch.name)
        }

        // 2. Starts with match
        val startsWithMatch = apps.find { it.name.lowercase().startsWith(cleanQuery) }
        if (startsWithMatch != null) {
            val launched = launchApp(startsWithMatch.packageName)
            return Pair(launched, startsWithMatch.name)
        }

        // 3. Contains match
        val containsMatch = apps.find { it.name.lowercase().contains(cleanQuery) }
        if (containsMatch != null) {
            val launched = launchApp(containsMatch.packageName)
            return Pair(launched, containsMatch.name)
        }

        return Pair(false, null)
    }
}
