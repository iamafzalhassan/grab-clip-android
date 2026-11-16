package com.example.grabclip.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.grabclip.domain.video.QualityPreference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val KEY_DELETE_AFTER_SHARING: String = "delete_after_sharing"
private const val KEY_ENGINE_UPDATED_AT: String = "engine_updated_at"
private const val KEY_PREFERRED_QUALITY: String = "preferred_quality"
private const val PREFERENCES_NAME: String = "settings"

class SettingsPreferences(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun engineUpdatedAt(): Long = preferences.getLong(KEY_ENGINE_UPDATED_AT, 0L)

    fun observeDeleteAfterSharing(): Flow<Boolean> = observe(key = KEY_DELETE_AFTER_SHARING, read = ::deleteAfterSharing)

    fun deleteAfterSharing(): Boolean = preferences.getBoolean(KEY_DELETE_AFTER_SHARING, true)

    fun observePreferredQuality(): Flow<Int> = observe(key = KEY_PREFERRED_QUALITY, read = ::preferredQuality)

    fun preferredQuality(): Int = preferences.getInt(KEY_PREFERRED_QUALITY, QualityPreference.DEFAULT).takeIf { preference -> preference in QualityPreference.options } ?: QualityPreference.DEFAULT

    fun setDeleteAfterSharing(isEnabled: Boolean) = preferences.edit { putBoolean(KEY_DELETE_AFTER_SHARING, isEnabled) }

    fun setEngineUpdatedAt(millis: Long) = preferences.edit { putLong(KEY_ENGINE_UPDATED_AT, millis) }

    fun setPreferredQuality(preference: Int) = preferences.edit { putInt(KEY_PREFERRED_QUALITY, preference) }

    private fun <T> observe(key: String, read: () -> T): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                trySend(read())
            }
        }
        trySend(read())
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}
