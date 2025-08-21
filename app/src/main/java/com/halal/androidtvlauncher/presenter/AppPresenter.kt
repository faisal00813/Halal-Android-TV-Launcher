package com.halal.androidtvlauncher.presenter

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.halal.androidtvlauncher.R
import com.halal.androidtvlauncher.model.AppInfo

class AppPresenter : Presenter() {
    
    companion object {
        private const val TAG = "AppPresenter"
        private const val CARD_WIDTH = 200
        private const val CARD_HEIGHT = 200
    }
    
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        try {
            val cardView = ImageCardView(parent.context).apply {
                isFocusable = true
                isFocusableInTouchMode = true
                setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            }
            return ViewHolder(cardView)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating view holder", e)
            // Return a simple view holder as fallback
            val fallbackView = ImageCardView(parent.context)
            return ViewHolder(fallbackView)
        }
    }
    
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        try {
            val appInfo = item as AppInfo
            val cardView = viewHolder.view as ImageCardView
            
            cardView.titleText = appInfo.appName
            cardView.contentText = appInfo.packageName
            
            // Safe icon loading with fallback
            try {
                appInfo.appIcon?.let { icon ->
                    cardView.mainImage = icon
                } ?: run {
                    // Set default icon if app icon is null
                    val defaultIcon = cardView.context.getDrawable(R.drawable.default_app_icon)
                    if (defaultIcon != null) {
                        cardView.mainImage = defaultIcon
                    } else {
                        // Create a simple fallback icon
                        cardView.mainImage = createFallbackIcon(cardView.context)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load app icon", e)
                cardView.mainImage = createFallbackIcon(cardView.context)
            }
            
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder", e)
            // Set minimal content to prevent crash
            try {
                val cardView = viewHolder.view as ImageCardView
                cardView.titleText = "Error"
                cardView.contentText = "Failed to load app"
                cardView.mainImage = createFallbackIcon(cardView.context)
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Failed to set fallback content", fallbackError)
            }
        }
    }
    
    private fun createFallbackIcon(context: Context): Drawable {
        return try {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(context.getColor(R.color.primary_color))
            shape.cornerRadius = context.resources.getDimensionPixelSize(R.dimen.icon_corner_radius).toFloat()
            shape
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create fallback icon", e)
            // Create a simple colored rectangle as last resort
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.setColor(context.getColor(R.color.primary_color))
            shape
        }
    }
    
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        try {
            val cardView = viewHolder.view as ImageCardView
            cardView.mainImage = null
            cardView.badgeImage = null
        } catch (e: Exception) {
            Log.w(TAG, "Error unbinding view holder", e)
        }
    }
} 