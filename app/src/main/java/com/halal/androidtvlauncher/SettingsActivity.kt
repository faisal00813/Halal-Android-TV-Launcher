package com.halal.androidtvlauncher

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.halal.androidtvlauncher.R

class SettingsActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SettingsActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_settings)
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_fragment, SettingsFragment())
                .commit()
                
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate", e)
            showErrorScreen("Failed to initialize settings: ${e.message}")
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
    
    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            try {
                setPreferencesFromResource(R.xml.preferences, rootKey)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load preferences", e)
                // Show error message
                Toast.makeText(context, "Failed to load settings: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
} 