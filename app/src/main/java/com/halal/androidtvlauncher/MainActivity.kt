package com.halal.androidtvlauncher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
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
        private const val REQUEST_LAUNCHER_ROLE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            
            // Ensure the layout is properly loaded before proceeding
            val mainLayout = findViewById<android.view.View>(R.id.main_browse_fragment)
            if (mainLayout == null) {
                Log.e(TAG, "Main layout not found, showing error screen")
                showErrorScreen("Failed to load launcher layout")
                return
            }
            
            loadInstalledApps()
            setupUI()
            
            // Check launcher role and prompt if needed
            checkLauncherRole()
            
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate", e)
            showErrorScreen("Failed to initialize launcher: ${e.message}")
        }
    }

    private fun checkLauncherRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                val isLauncher = roleManager.isRoleHeld(RoleManager.ROLE_HOME)
                
                if (!isLauncher) {
                    Log.d(TAG, "App is not default launcher, showing role request")
                    // Show role request after a longer delay to ensure UI is fully loaded
                    findViewById<android.view.View>(android.R.id.content).postDelayed({
                        try {
                            showLauncherRoleRequest()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error showing launcher role request", e)
                        }
                    }, 5000) // Increased delay to 5 seconds
                } else {
                    Log.d(TAG, "App is already default launcher")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking launcher role", e)
            }
        }
    }
    
    private fun showLauncherRoleRequest() {
        try {
            // Check if the activity is still valid before starting
            if (!isFinishing && !isDestroyed) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
                    if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                        startActivityForResult(intent, REQUEST_LAUNCHER_ROLE)
                    } else {
                        Log.w(TAG, "Launcher role not available, showing manual setup message")
                        showFallbackRoleMessage()
                    }
                } else {
                    // For older Android versions, show manual setup message
                    showFallbackRoleMessage()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing launcher role request", e)
            // Fallback: show a simple toast message
            showFallbackRoleMessage()
        }
    }
    
    private fun showFallbackRoleMessage() {
        try {
            Toast.makeText(
                this,
                "To set as default launcher, go to Settings > Apps > Default apps > Home app",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing fallback message", e)
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
            
            // Hide loading indicator and show main content
            hideLoadingIndicator()
                
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in setupUI", e)
            showErrorScreen("Failed to setup launcher interface")
        }
    }
    
    private fun hideLoadingIndicator() {
        try {
            val loadingLayout = findViewById<android.view.View>(R.id.loading_layout)
            val mainFragment = findViewById<android.view.View>(R.id.main_browse_fragment)
            
            loadingLayout?.visibility = android.view.View.GONE
            mainFragment?.visibility = android.view.View.VISIBLE
        } catch (e: Exception) {
            Log.w(TAG, "Error hiding loading indicator", e)
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
            // Get user preference for showing system apps
            val showSystemApps = getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
                .getBoolean("show_system_apps", true)
            
            installedApps = if (showSystemApps) {
                AppUtils.getInstalledApps(this)
            } else {
                AppUtils.getInstalledAppsExcludingSystem(this)
            }
            
            Log.d(TAG, "Loaded ${installedApps.size} apps (showSystemApps: $showSystemApps)")

            // Log first few apps for debugging
            installedApps.take(5).forEach { app ->
                Log.d(TAG, "App: ${app.appName} (${app.packageName}) - System: ${app.isSystemApp}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading apps", e)
            Toast.makeText(this, "Error loading apps: ${e.message}", Toast.LENGTH_SHORT).show()
            // Set empty list to prevent crashes
            installedApps = emptyList()
        }
    }
    
    private fun refreshAppList() {
        try {
            loadInstalledApps()
            
            // Update the adapter
            if (::appsRowAdapter.isInitialized) {
                appsRowAdapter.clear()
                appsRowAdapter.addAll(0, installedApps)
            }
            
            Toast.makeText(this, getString(R.string.app_list_refreshed, installedApps.size), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing app list", e)
            Toast.makeText(this, R.string.error_refreshing_apps, Toast.LENGTH_SHORT).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_LAUNCHER_ROLE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Launcher role granted successfully")
                Toast.makeText(this, "Halal Android TV Launcher is now your default launcher!", Toast.LENGTH_LONG).show()
            } else {
                Log.d(TAG, "Launcher role request denied or cancelled")
                Toast.makeText(this, "To set as default launcher, go to Settings > Apps > Default apps > Home app", Toast.LENGTH_LONG).show()
            }
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
            KeyEvent.KEYCODE_HOME -> {
                // Handle home button press
                Log.d(TAG, "Home button pressed")
                true
            }
            KeyEvent.KEYCODE_R -> {
                // Refresh app list (R key)
                refreshAppList()
                true
            }
            KeyEvent.KEYCODE_S -> {
                // Toggle system app visibility (S key)
                toggleSystemAppVisibility()
                true
            }
            KeyEvent.KEYCODE_H -> {
                // Show help overlay (H key)
                showHelpOverlay()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
    
    private fun showHelpOverlay() {
        try {
            val helpView = layoutInflater.inflate(R.layout.help_overlay, null)
            val dialog = android.app.AlertDialog.Builder(this)
                .setView(helpView)
                .setCancelable(true)
                .create()
            
            dialog.show()
            
            // Auto-dismiss after 10 seconds
            helpView.postDelayed({
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }, 10000)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing help overlay", e)
            Toast.makeText(this, R.string.error_showing_help, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun toggleSystemAppVisibility() {
        try {
            val prefs = getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
            val currentSetting = prefs.getBoolean("show_system_apps", true)
            val newSetting = !currentSetting
            
            prefs.edit().putBoolean("show_system_apps", newSetting).apply()
            
            // Refresh the app list with new setting
            refreshAppList()
            
            val message = if (newSetting) getString(R.string.system_apps_on) else getString(R.string.system_apps_off)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling system app visibility", e)
            Toast.makeText(this, R.string.error_toggling_system_apps, Toast.LENGTH_SHORT).show()
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
    
    override fun onResume() {
        super.onResume()
        // Check launcher role again when returning to the app
        checkLauncherRole()
    }
}
