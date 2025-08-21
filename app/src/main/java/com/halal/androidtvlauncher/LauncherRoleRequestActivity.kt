package com.halal.androidtvlauncher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LauncherRoleRequestActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "LauncherRoleRequest"
        private const val REQUEST_LAUNCHER_ROLE = 1001
    }
    
    private lateinit var titleText: TextView
    private lateinit var messageText: TextView
    private lateinit var requestButton: Button
    private lateinit var manualButton: Button
    private lateinit var skipButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_launcher_role_request)
            initializeViews()
            setupClickListeners()
            checkLauncherRole()
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate", e)
            showErrorScreen("Failed to initialize role request: ${e.message}")
        }
    }
    
    private fun initializeViews() {
        try {
            titleText = findViewById(R.id.role_request_title) ?: throw IllegalStateException("Title view not found")
            messageText = findViewById(R.id.role_request_message) ?: throw IllegalStateException("Message view not found")
            requestButton = findViewById(R.id.request_role_button) ?: throw IllegalStateException("Request button not found")
            manualButton = findViewById(R.id.manual_setup_button) ?: throw IllegalStateException("Manual button not found")
            skipButton = findViewById(R.id.skip_button) ?: throw IllegalStateException("Skip button not found")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }
    
    private fun setupClickListeners() {
        try {
            requestButton.setOnClickListener {
                requestLauncherRole()
            }
            
            manualButton.setOnClickListener {
                openLauncherSettings()
            }
            
            skipButton.setOnClickListener {
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }
    
    private fun checkLauncherRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
                if (roleManager != null) {
                    val isLauncher = roleManager.isRoleHeld(RoleManager.ROLE_HOME)
                    
                    if (isLauncher) {
                        titleText.text = getString(R.string.launcher_already_set)
                        messageText.text = getString(R.string.launcher_already_set_message)
                        requestButton.visibility = View.GONE
                        manualButton.visibility = View.GONE
                    } else {
                        titleText.text = getString(R.string.set_as_default_launcher)
                        messageText.text = getString(R.string.default_launcher_explanation)
                    }
                } else {
                    Log.w(TAG, "RoleManager service not available")
                    showManualSetupOnly()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking launcher role", e)
                showManualSetupOnly()
            }
        } else {
            Log.d(TAG, "Android version < Q, showing manual setup only")
            showManualSetupOnly()
        }
    }
    
    private fun showManualSetupOnly() {
        try {
            titleText.text = getString(R.string.manual_setup_required)
            messageText.text = getString(R.string.manual_setup_explanation)
            requestButton.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing manual setup only", e)
        }
    }
    
    private fun requestLauncherRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
                
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    startActivityForResult(intent, REQUEST_LAUNCHER_ROLE)
                } else {
                    Log.w(TAG, "Launcher role not available")
                    Toast.makeText(this, R.string.launcher_role_not_available, Toast.LENGTH_SHORT).show()
                    openLauncherSettings()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting launcher role", e)
                Toast.makeText(this, R.string.error_requesting_role, Toast.LENGTH_SHORT).show()
                openLauncherSettings()
            }
        } else {
            // For older Android versions, open settings directly
            openLauncherSettings()
        }
    }
    
    private fun openLauncherSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback to general settings
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening launcher settings", e)
            Toast.makeText(this, R.string.error_opening_settings, Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_LAUNCHER_ROLE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Launcher role granted successfully")
                Toast.makeText(this, R.string.launcher_role_granted, Toast.LENGTH_LONG).show()
                
                // Update UI to show success
                try {
                    titleText.text = getString(R.string.launcher_role_granted_title)
                    messageText.text = getString(R.string.launcher_role_granted_message)
                    requestButton.visibility = View.GONE
                    manualButton.visibility = View.GONE
                    
                    // Auto-finish after delay
                    skipButton.postDelayed({
                        finish()
                    }, 3000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating UI after role granted", e)
                }
                
            } else {
                Log.d(TAG, "Launcher role request denied or cancelled")
                Toast.makeText(this, R.string.launcher_role_denied, Toast.LENGTH_SHORT).show()
                
                // Show manual setup option
                try {
                    titleText.text = getString(R.string.manual_setup_required)
                    messageText.text = getString(R.string.manual_setup_explanation)
                    requestButton.visibility = View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating UI after role denied", e)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check if role was granted while settings were open
        try {
            checkLauncherRole()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking launcher role on resume", e)
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
            finish()
        }
    }
} 