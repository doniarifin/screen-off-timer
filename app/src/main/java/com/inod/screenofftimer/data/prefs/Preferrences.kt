package com.inod.screenofftimer.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.inod.screenofftimer.ui.enums.ThemeMode
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

object Prefs {
    private const val NAME = "app_prefs"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_LOCK_SCREEN = "lock_screen"
    private const val KEY_STOP_MEDIA = "stop_media"
    private const val KEY_ACCESSIBILITY = "accessibility"

    object Keys {
        const val LEFT_SECONDS = "left_seconds"
        const val MINUTES = "minutes"
        const val IS_RUNNING = "is_running"
    }

    data class Settings(
        val leftSeconds: Int = 0,
        val minutes: Int = 0,
        val isRunning: Boolean = false
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)


    // left_seconds
    fun saveLeftSeconds(context: Context, seconds: Int) {
        prefs(context).edit { putInt(Keys.LEFT_SECONDS, seconds) }
    }

    fun getLeftSeconds(context: Context): Int {
        return prefs(context).getInt(Keys.LEFT_SECONDS, 300)
    }

    //minutes
    fun saveMinutes(context: Context, minutes: Int) {
        prefs(context).edit { putInt(Keys.MINUTES, minutes) }
    }

    fun getMinutes(context: Context): Int {
        return prefs(context).getInt(Keys.MINUTES, 0)
    }

    // is_running
    fun saveRunning(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(Keys.IS_RUNNING, value) }
    }

    fun isRunning(context: Context): Boolean {
        return prefs(context).getBoolean(Keys.IS_RUNNING, false)
    }

    fun runningFlow(context: Context): Flow<Boolean> = callbackFlow {
        val prefs = prefs(context)

        trySend(prefs.getBoolean(Keys.IS_RUNNING, false))

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == Keys.IS_RUNNING) {
                trySend(prefs.getBoolean(Keys.IS_RUNNING, false))
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    // theme
    fun saveTheme(context: Context, theme: ThemeMode) {
        prefs(context).edit { putString(KEY_THEME, theme.name) }
    }

    fun getTheme(context: Context): ThemeMode {
        val value = prefs(context).getString(KEY_THEME, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(value ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    // accessibility
    fun saveAccessibility(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_ACCESSIBILITY, value) }
    }

    fun isAccessibility(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ACCESSIBILITY, false)
    }

    // lock_screen
    fun saveLockScreen(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_LOCK_SCREEN, value) }
    }

    fun isLockScreen(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_LOCK_SCREEN, false)
    }

    // stop media
    fun saveStopMedia(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_STOP_MEDIA, value) }
    }

    fun isStopMedia(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_STOP_MEDIA, false)
    }
}