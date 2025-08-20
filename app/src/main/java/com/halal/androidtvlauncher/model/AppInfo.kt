package com.halal.androidtvlauncher.model

import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val installTime: Long,
    val updateTime: Long,
    val category: String = "",
    val isFavorite: Boolean = false
) : Parcelable {
    
    // Transient property for the app icon (not parcelized)
    @Transient
    var appIcon: Drawable? = null
        private set
    
    // Secondary constructor to include app icon
    constructor(
        packageName: String,
        appName: String,
        appIcon: Drawable?,
        versionName: String,
        versionCode: Long,
        isSystemApp: Boolean,
        installTime: Long,
        updateTime: Long,
        category: String = "",
        isFavorite: Boolean = false
    ) : this(packageName, appName, versionName, versionCode, isSystemApp, installTime, updateTime, category, isFavorite) {
        this.appIcon = appIcon
    }
    
    // Method to update the app icon
    fun updateAppIcon(icon: Drawable?) {
        appIcon = icon
    }
} 