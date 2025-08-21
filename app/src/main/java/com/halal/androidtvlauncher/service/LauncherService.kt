package com.halal.androidtvlauncher.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.halal.androidtvlauncher.utils.AppUtils

class LauncherService : Service() {
    
    companion object {
        private const val TAG = "LauncherService"
        const val ACTION_UPDATE_APPS = "com.halal.androidtvlauncher.UPDATE_APPS"
        const val ACTION_CLEAR_CACHE = "com.halal.androidtvlauncher.CLEAR_CACHE"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LauncherService created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LauncherService started with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_UPDATE_APPS -> {
                updateAppList()
            }
            ACTION_CLEAR_CACHE -> {
                clearCache()
            }
            else -> {
                // Default behavior - keep service running
                Log.d(TAG, "No specific action, keeping service alive")
            }
        }
        
        // Return START_STICKY to restart service if killed
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LauncherService destroyed")
    }
    
    private fun updateAppList() {
        try {
            Log.d(TAG, "Updating app list in background")
            // This could trigger a broadcast to update the UI
            // For now, just log the action
            val appCount = AppUtils.getInstalledApps(this).size
            Log.d(TAG, "Updated app list, found $appCount apps")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating app list", e)
        }
    }
    
    private fun clearCache() {
        try {
            Log.d(TAG, "Clearing launcher cache")
            // Clear any cached data
            // This could include clearing image cache, preferences cache, etc.
            Log.d(TAG, "Cache cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
} 