package com.halal.androidtvlauncher

import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.halal.androidtvlauncher.presenter.AppPresenter
import com.halal.androidtvlauncher.model.AppInfo
import com.halal.androidtvlauncher.utils.AppUtils

class MainActivity : FragmentActivity() {

    private lateinit var browseSupportFragment: BrowseSupportFragment
    private var installedApps: List<AppInfo> = emptyList()
    private lateinit var appsRowAdapter: ArrayObjectAdapter

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            loadInstalledApps()
            setupUI()
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate", e)
            showErrorScreen("Failed to initialize launcher: ${e.message}")
        }
    }

    private fun setupUI() {
        try {
            browseSupportFragment = BrowseSupportFragment()
            browseSupportFragment.title = getString(R.string.app_name)
            
            // Safe resource loading with fallback
            try {
                browseSupportFragment.badgeDrawable = getDrawable(R.drawable.app_banner)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load app banner, using default", e)
                browseSupportFragment.badgeDrawable = createFallbackBanner()
            }

            // Set up the main grid using ArrayObjectAdapter
            val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

            // Create a row for all apps
            appsRowAdapter = ArrayObjectAdapter(AppPresenter())
            rowsAdapter.add(ListRow(HeaderItem("All Apps"), appsRowAdapter))

            // Add all apps to the adapter
            appsRowAdapter.addAll(0, installedApps)

            // Set the rows adapter
            browseSupportFragment.adapter = rowsAdapter

            // Handle item clicks
            browseSupportFragment.onItemViewClickedListener =
                OnItemViewClickedListener { _, item, _, _ ->
                    if (item is AppInfo) {
                        Log.d(TAG, "Launching app: ${item.appName} (${item.packageName})")
                        launchApp(item)
                    } else {
                        Log.w(TAG, "Clicked item is not AppInfo: ${item?.javaClass?.simpleName}")
                    }
                }

            // Handle item selection
            browseSupportFragment.onItemViewSelectedListener =
                OnItemViewSelectedListener { _, item, _, _ ->
                    Log.d(TAG, "Item selected: $item")
                }

            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, browseSupportFragment)
                .commit()
                
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in setupUI", e)
            showErrorScreen("Failed to setup launcher interface")
        }
    }

    private fun createFallbackBanner(): Drawable {
        return try {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(getColor(R.color.primary_color))
            shape.cornerRadius = resources.getDimensionPixelSize(R.dimen.banner_corner_radius).toFloat()
            shape
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create fallback banner", e)
            // Create a simple colored rectangle as last resort
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(getColor(R.color.primary_color))
            shape
        }
    }

    private fun loadInstalledApps() {
        try {
            installedApps = AppUtils.getInstalledApps(this)
            Log.d(TAG, "Loaded ${installedApps.size} apps")

            // Log first few apps for debugging
            installedApps.take(5).forEach { app ->
                Log.d(TAG, "App: ${app.appName} (${app.packageName})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading apps", e)
            Toast.makeText(this, "Error loading apps: ${e.message}", Toast.LENGTH_SHORT).show()
            // Set empty list to prevent crashes
            installedApps = emptyList()
        }
    }

    private fun launchApp(appInfo: AppInfo) {
        try {
            Log.d(TAG, "Attempting to launch: ${appInfo.packageName}")
            val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this, "Launching ${appInfo.appName}", Toast.LENGTH_SHORT).show()
            } else {
                Log.w(TAG, "No launch intent found for: ${appInfo.packageName}")
                Toast.makeText(this, "Cannot launch ${appInfo.appName}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${appInfo.appName}", e)
            Toast.makeText(this, "Error launching ${appInfo.appName}: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorScreen(message: String) {
        try {
            // Show a simple error message instead of crashing
            val errorView = TextView(this).apply {
                text = message
                setTextColor(getColor(R.color.error_color))
                gravity = Gravity.CENTER
                textSize = 18f
                setBackgroundColor(getColor(R.color.background_color))
            }
            setContentView(errorView)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show error screen", e)
            // Last resort - show system error
            Toast.makeText(this, "Critical error: $message", Toast.LENGTH_LONG).show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                openAppDrawer()
                true
            }
            KeyEvent.KEYCODE_SETTINGS -> {
                openSettings()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun openAppDrawer() {
        try {
            val intent = Intent(this, AppDrawerActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app drawer", e)
            Toast.makeText(this, "Failed to open app drawer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSettings() {
        try {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open settings", e)
            Toast.makeText(this, "Failed to open settings", Toast.LENGTH_SHORT).show()
        }
    }
}
