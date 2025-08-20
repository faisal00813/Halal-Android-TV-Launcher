package com.halal.androidtvlauncher.presenter

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.halal.androidtvlauncher.R
import com.halal.androidtvlauncher.model.AppInfo

class AppPresenter : Presenter() {
    
    companion object {
        private const val CARD_WIDTH = 200
        private const val CARD_HEIGHT = 200
    }
    
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        }
        return ViewHolder(cardView)
    }
    
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val appInfo = item as AppInfo
        val cardView = viewHolder.view as ImageCardView
        
        cardView.titleText = appInfo.appName
        cardView.contentText = appInfo.packageName
        
        // Load app icon
        appInfo.appIcon?.let { icon ->
            cardView.mainImage = icon
        } ?: run {
            // Set default icon if app icon is null
            cardView.mainImage = cardView.context.getDrawable(R.drawable.default_app_icon)
        }
        
        // Set card background
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
    }
    
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImage = null
        cardView.badgeImage = null
    }
} 