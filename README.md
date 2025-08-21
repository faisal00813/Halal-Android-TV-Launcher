# Halal Android TV Launcher

A modern, customizable Android TV launcher template built with Kotlin and the Leanback library.

## Project Structure

```
app/
├── src/main/
│   ├── java/com/halal/androidtvlauncher/
│   │   ├── MainActivity.kt              # Main launcher activity
│   │   ├── AppDrawerActivity.kt         # App drawer activity
│   │   ├── SettingsActivity.kt          # Settings activity
│   │   ├── adapter/
│   │   │   └── AppAdapter.kt            # App grid adapter
│   │   ├── model/
│   │   │   └── AppInfo.kt               # App information model
│   │   ├── presenter/
│   │   │   └── AppPresenter.kt          # App item presenter
│   │   ├── provider/
│   │   │   └── TVProvider.kt            # TV content provider
│   │   └── utils/
│   │       └── AppUtils.kt              # Utility functions
│   ├── res/
│   │   ├── layout/                      # Activity layouts
│   │   ├── values/                      # Resources (strings, colors, themes)
│   │   ├── xml/                         # Preferences and rules
│   │   └── drawable/                    # Icons and graphics
│   └── AndroidManifest.xml              # App manifest
├── build.gradle                         # App module build configuration
└── proguard-rules.pro                   # ProGuard rules

build.gradle                             # Project-level build configuration
settings.gradle                          # Project settings
gradle.properties                        # Gradle properties
gradle/wrapper/                          # Gradle wrapper files
```

## Key Components

### MainActivity
The main launcher interface that displays the app grid and handles navigation.

### AppAdapter
Manages the display of installed applications in a grid layout with sorting and filtering capabilities.

### AppPresenter
Handles the visual presentation of app items using ImageCardView for TV-optimized display.

### AppUtils
Utility functions for retrieving installed app information and categorization.

### Settings
Comprehensive settings panel for customizing launcher appearance and behavior.

## Dependencies

- **AndroidX Leanback**: TV-specific UI components
- **AndroidX TV**: Modern TV UI foundation
- **Glide**: Image loading and caching
- **Kotlin Coroutines**: Asynchronous programming
- **Navigation Component**: Activity navigation

## Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on an Android TV device or emulator

## Customization

### Themes
Modify `res/values/themes.xml` to customize the launcher appearance.

### Colors
Update `res/values/colors.xml` to change the color scheme.

### Preferences
Edit `res/xml/preferences.xml` to modify available settings options.

### App Categories
Update the `getAppCategory()` function in `AppUtils.kt` to customize app categorization.
