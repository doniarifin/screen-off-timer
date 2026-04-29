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
    private const val KEY_GO_HOME = "is_go_home"
    private const val KEY_DYNAMIC_COLOR = "is_dynamic_color"
    private const val KEY_ACCESSIBILITY = "accessibility"
    private const val KEY_NOTIF_PERMISSION = "notif_permission"
    private const val KEY_NO_SHOW_ASK_NOTIF = "no_show_ask_notif"
    private const val KEY_LEFT_SECONDS = "left_seconds"
    private const val KEY_MINUTES = "minutes"
    private const val KEY_IS_RUNNING = "is_running"

    data class AllSettings(
        val theme: ThemeMode = ThemeMode.SYSTEM,
        val isRunning: Boolean = false,
        val leftSeconds: Int = 300,
        val minutes: Int = 0,
        val accessibility: Boolean = false,
        val isLockScreen: Boolean = false,
        val isStopMedia: Boolean = false,
        val isGoHome: Boolean = false,
        val isDynamicColor: Boolean = false,
        val isNotifPermission: Boolean = false,
        val isNoShowNotif: Boolean = false
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    // coroutine
    fun getAllSettings(context: Context): AllSettings {
        val all = prefs(context).all
        return AllSettings(
            theme = try {
                ThemeMode.valueOf(all[KEY_THEME] as? String ?: "SYSTEM")
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            },
            isRunning = all[KEY_IS_RUNNING] as? Boolean ?: false,
            leftSeconds = all[KEY_LEFT_SECONDS] as? Int ?: 300,
            minutes = all[KEY_MINUTES] as? Int ?: 0,
            accessibility = all[KEY_ACCESSIBILITY] as? Boolean ?: false,
            isLockScreen = all[KEY_LOCK_SCREEN] as? Boolean ?: false,
            isStopMedia = all[KEY_STOP_MEDIA] as? Boolean ?: false,
            isGoHome = all[KEY_GO_HOME] as? Boolean ?: false,
            isDynamicColor = all[KEY_DYNAMIC_COLOR] as? Boolean ?: false,
            isNotifPermission = all[KEY_NOTIF_PERMISSION] as? Boolean ?: false,
            isNoShowNotif = all[KEY_NO_SHOW_ASK_NOTIF] as? Boolean ?: false
        )
    }

    fun settingsFlow(context: Context): Flow<AllSettings> = callbackFlow {
        val prefs = prefs(context)

        trySend(getAllSettings(context))

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(getAllSettings(context))
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    //function save and get
    fun saveLeftSeconds(context: Context, seconds: Int) {
        prefs(context).edit { putInt(KEY_LEFT_SECONDS, seconds) }
    }

    fun getLeftSeconds(context: Context): Int = prefs(context).getInt(KEY_LEFT_SECONDS, 300)

    fun saveMinutes(context: Context, minutes: Int) {
        prefs(context).edit { putInt(KEY_MINUTES, minutes) }
    }

    fun getMinutes(context: Context): Int = prefs(context).getInt(KEY_MINUTES, 0)

    fun saveRunning(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_IS_RUNNING, value) }
    }

    fun isRunning(context: Context): Boolean = prefs(context).getBoolean(KEY_IS_RUNNING, false)

    fun runningFlow(context: Context): Flow<Boolean> = callbackFlow {
        val prefs = prefs(context)
        trySend(prefs.getBoolean(KEY_IS_RUNNING, false))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == KEY_IS_RUNNING) {
                trySend(p.getBoolean(KEY_IS_RUNNING, false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

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

    fun saveAccessibility(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_ACCESSIBILITY, value) }
    }

    fun isAccessibility(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ACCESSIBILITY, false)

    fun saveLockScreen(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_LOCK_SCREEN, value) }
    }

    fun isLockScreen(context: Context): Boolean = prefs(context).getBoolean(KEY_LOCK_SCREEN, false)

    fun saveStopMedia(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_STOP_MEDIA, value) }
    }

    fun isStopMedia(context: Context): Boolean = prefs(context).getBoolean(KEY_STOP_MEDIA, false)

    fun saveGoHome(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_GO_HOME, value) }
    }

    fun isGoHome(context: Context): Boolean = prefs(context).getBoolean(KEY_GO_HOME, false)

    fun saveDynamicColor(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_DYNAMIC_COLOR, value) }
    }

    fun isDynamicColor(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DYNAMIC_COLOR, false)

    fun saveNotifPermission(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_NOTIF_PERMISSION, value) }
    }

    fun isNotifPermission(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NOTIF_PERMISSION, false)

    fun saveNoShowNotifPermission(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_NO_SHOW_ASK_NOTIF, value) }
    }

    fun isNoShowNotifPermission(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NO_SHOW_ASK_NOTIF, false)
}