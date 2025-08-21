package com.halal.androidtvlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.halal.androidtvlauncher.MainActivity

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "Boot completed, checking launcher status")
                handleBootCompleted(context)
            }
        }
    }

    private fun handleBootCompleted(context: Context) {
        try {
            // Check if this app is the default launcher
            if (isDefaultLauncher(context)) {
                Log.d(TAG, "App is default launcher, starting MainActivity")
                startLauncher(context)
            } else {
                Log.d(TAG, "App is not default launcher, skipping auto-start")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling boot completed", e)
        }
    }

    private fun isDefaultLauncher(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = context.packageManager.resolveActivity(intent, 0)
            resolveInfo?.activityInfo?.packageName == context.packageName
        } catch (e: Exception) {
            Log.e(TAG, "Error checking default launcher", e)
            false
        }
    }

    private fun startLauncher(context: Context) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
            Log.d(TAG, "Launcher started successfully after boot")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting launcher after boot", e)
        }
    }
}
