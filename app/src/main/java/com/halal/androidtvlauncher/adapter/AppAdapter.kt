package com.halal.androidtvlauncher.adapter

import android.content.Context
import androidx.leanback.widget.*
import com.halal.androidtvlauncher.model.AppInfo
import com.halal.androidtvlauncher.presenter.AppPresenter

class AppAdapter(
    private val context: Context,
    private var apps: List<AppInfo>
) : ArrayObjectAdapter(AppPresenter()) {
    
    init {
        updateApps(apps)
    }
    
    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        clear()
        addAll(0, apps)
    }
    
    fun getApps(): List<AppInfo> = apps
    
    fun filterApps(query: String) {
        val filteredApps = if (query.isEmpty()) {
            apps
        } else {
            apps.filter { 
                it.appName.contains(query, ignoreCase = true) || 
                it.packageName.contains(query, ignoreCase = true) 
            }
        }
        updateApps(filteredApps)
    }
    
    fun sortByAppName() {
        val sortedApps = apps.sortedBy { it.appName }
        updateApps(sortedApps)
    }
    
    fun sortByInstallTime() {
        val sortedApps = apps.sortedByDescending { it.installTime }
        updateApps(sortedApps)
    }
    
    fun sortByCategory() {
        val sortedApps = apps.sortedBy { it.category }
        updateApps(sortedApps)
    }
    
    fun getFavorites(): List<AppInfo> {
        return apps.filter { it.isFavorite }
    }
} 