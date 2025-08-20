package com.halal.androidtvlauncher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.halal.androidtvlauncher.presenter.AppPresenter
import com.halal.androidtvlauncher.model.AppInfo
import com.halal.androidtvlauncher.utils.AppUtils

class AppDrawerActivity : AppCompatActivity() {
    
    private lateinit var browseSupportFragment: BrowseSupportFragment
    private var installedApps: List<AppInfo> = emptyList()
    private lateinit var appsRowAdapter: ArrayObjectAdapter
    
    companion object {
        private const val TAG = "AppDrawerActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_drawer)
        
        loadInstalledApps()
        setupUI()
    }
    
    private fun setupUI() {
        browseSupportFragment = BrowseSupportFragment()
        browseSupportFragment.title = "App Drawer"
        browseSupportFragment.badgeDrawable = getDrawable(R.drawable.app_banner)
        
        // Set up the main grid using ArrayObjectAdapter
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        
        // Create a row for all apps
        appsRowAdapter = ArrayObjectAdapter(AppPresenter())
        rowsAdapter.add(ListRow(HeaderItem("All Apps"), appsRowAdapter))
        
        // Add all apps to the adapter
        appsRowAdapter.addAll(0, installedApps)
        
        // Set the rows adapter
        browseSupportFragment.adapter = rowsAdapter
        
        // Handle item selection
        browseSupportFragment.onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            Log.d(TAG, "Item clicked: $item")
            if (item is AppInfo) {
                Log.d(TAG, "Launching app: ${item.appName} (${item.packageName})")
                launchApp(item)
            } else {
                Log.w(TAG, "Clicked item is not AppInfo: ${item?.javaClass?.simpleName}")
            }
        }
        
        // Handle item selection for long press (context menu)
        browseSupportFragment.onItemViewSelectedListener = OnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            // Handle selection for visual feedback
            Log.d(TAG, "Item selected: $item")
        }
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.app_drawer_fragment, browseSupportFragment)
            .commit()
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
        }
    }
    
    private fun launchApp(appInfo: AppInfo) {
        try {
            Log.d(TAG, "Attempting to launch: ${appInfo.packageName}")
            
            val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Log.d(TAG, "Successfully launched: ${appInfo.appName}")
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
} 