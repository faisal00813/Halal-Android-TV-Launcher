package com.halal.androidtvlauncher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import com.halal.androidtvlauncher.model.AppInfo

object AppUtils {
    
    private const val TAG = "AppUtils"
    
    fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val installedApps = mutableListOf<AppInfo>()
        
        try {
            val packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            Log.d(TAG, "Total packages found: ${packages.size}")
            
            for (packageInfo in packages) {
                try {
                    val appInfo = packageInfo.applicationInfo
                    
                    // Check if the app has a launch intent (can be launched)
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageInfo.packageName)
                    if (launchIntent == null) {
                        Log.d(TAG, "Skipping ${packageInfo.packageName} - no launch intent")
                        continue
                    }
                    
                    // Skip some system apps but allow important ones
                    if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                        // Allow some important system apps
                        val packageName = packageInfo.packageName
                        if (!isImportantSystemApp(packageName)) {
                            Log.d(TAG, "Skipping system app: ${packageInfo.packageName}")
                            continue
                        }
                    }
                    
                    // Safe app name loading
                    val appName = try {
                        packageInfo.applicationInfo.loadLabel(packageManager)?.toString() ?: packageInfo.packageName
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load app name for ${packageInfo.packageName}, using package name", e)
                        packageInfo.packageName
                    }
                    
                    // Safe app icon loading
                    val appIcon = try {
                        packageInfo.applicationInfo.loadIcon(packageManager)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load app icon for ${packageInfo.packageName}", e)
                        null
                    }
                    
                    // Safe version info loading
                    val versionName = try {
                        packageInfo.versionName ?: ""
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load version name for ${packageInfo.packageName}", e)
                        ""
                    }
                    
                    val versionCode = try {
                        packageInfo.longVersionCode
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load version code for ${packageInfo.packageName}", e)
                        0L
                    }
                    
                    val app = AppInfo(
                        packageName = packageInfo.packageName,
                        appName = appName,
                        appIcon = appIcon,
                        versionName = versionName,
                        versionCode = versionCode,
                        isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                        installTime = packageInfo.firstInstallTime,
                        updateTime = packageInfo.lastUpdateTime,
                        category = getAppCategory(packageInfo.packageName)
                    )
                    
                    installedApps.add(app)
                    Log.d(TAG, "Added app: ${app.appName} (${app.packageName})")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading app: ${packageInfo.packageName}", e)
                    continue
                }
            }
            
            Log.d(TAG, "Total launchable apps found: ${installedApps.size}")
            return installedApps.sortedBy { it.appName }
            
        } catch (e: Exception) {
            Log.e(TAG, "Critical error getting installed packages", e)
            return emptyList()
        }
    }
    
    private fun isImportantSystemApp(packageName: String): Boolean {
        return when {
            packageName.startsWith("com.android.tv") -> true
            packageName.startsWith("com.google.android.tv") -> true
            packageName.startsWith("com.android.settings") -> true
            packageName.startsWith("com.android.launcher") -> true
            packageName.startsWith("com.google.android.apps.tv.launcherx") -> true
            packageName.startsWith("com.google.android.apps.tv.launcher") -> true
            packageName == "android" -> true
            packageName == "com.android.systemui" -> true
            else -> false
        }
    }
    
    private fun getAppCategory(packageName: String): String {
        return when {
            packageName.startsWith("com.android.tv") -> "TV Apps"
            packageName.startsWith("com.google.android.tv") -> "TV Apps"
            packageName.startsWith("com.netflix") -> "Streaming"
            packageName.startsWith("com.amazon.avod") -> "Streaming"
            packageName.startsWith("com.spotify") -> "Music"
            packageName.startsWith("com.google.android.youtube") -> "Video"
            packageName.startsWith("com.videolan.vlc") -> "Video"
            packageName.startsWith("com.mxtech.videoplayer") -> "Video"
            packageName.startsWith("com.estrongs.android.pop") -> "File Manager"
            packageName.startsWith("com.adobe.reader") -> "Document"
            packageName.startsWith("com.microsoft.office") -> "Office"
            packageName.startsWith("com.android.settings") -> "System"
            packageName.startsWith("com.android.launcher") -> "System"
            else -> "Other"
        }
    }
    
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if app is installed: $packageName", e)
            false
        }
    }
    
    fun getAppInfo(context: Context, packageName: String): AppInfo? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            val appInfo = packageInfo.applicationInfo
            
            // Safe app name loading
            val appName = try {
                packageInfo.applicationInfo.loadLabel(context.packageManager)?.toString() ?: packageName
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load app name for $packageName", e)
                packageName
            }
            
            // Safe app icon loading
            val appIcon = try {
                packageInfo.applicationInfo.loadIcon(context.packageManager)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load app icon for $packageName", e)
                null
            }
            
            AppInfo(
                packageName = packageInfo.packageName,
                appName = appName,
                appIcon = appIcon,
                versionName = packageInfo.versionName ?: "",
                versionCode = packageInfo.longVersionCode,
                isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                installTime = packageInfo.firstInstallTime,
                updateTime = packageInfo.lastUpdateTime,
                category = getAppCategory(packageInfo.packageName)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app info for $packageName", e)
            null
        }
    }
} 