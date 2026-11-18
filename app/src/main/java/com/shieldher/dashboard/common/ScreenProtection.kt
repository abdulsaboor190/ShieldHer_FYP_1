package com.shieldher.dashboard.common

import android.content.Context
import android.view.Window
import android.view.WindowManager

/**
 * Pure function determining whether FLAG_SECURE should be active.
 *
 * Decision D8: Screen protection is on by default, can be toggled by the user in Settings,
 * and is suppressed when demo mode is active.
 */
fun shouldSecure(settingEnabled: Boolean, demoModeActive: Boolean): Boolean =
    settingEnabled && !demoModeActive

/**
 * Global demo state flag (set by demo mode in FE-5.1, debug builds only).
 */
object DemoState {
    @Volatile
    var active: Boolean = false
}

/**
 * AppSettings backed by SharedPreferences for user-level settings.
 *
 * Strictly stores non-sensitive preferences only. No evidence data, paths, hashes,
 * or user text may ever be stored here.
 */
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("shieldher_settings", Context.MODE_PRIVATE)

    var screenshotProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCREENSHOT_PROTECTION, true)
        set(value) = prefs.edit().putBoolean(KEY_SCREENSHOT_PROTECTION, value).apply()

    companion object {
        private const val KEY_SCREENSHOT_PROTECTION = "screenshot_protection_enabled"
    }
}

/**
 * Applies or clears WindowManager.LayoutParams.FLAG_SECURE on the target [Window].
 */
fun applyTo(window: Window, settings: AppSettings) {
    val secure = shouldSecure(settings.screenshotProtectionEnabled, DemoState.active)
    if (secure) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
