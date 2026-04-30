package hf.inner.notebook.utils

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import hf.inner.notebook.App
import hf.inner.notebook.R
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val THEME_PREFERENCES = "THEME_PREFERENCES"

private val Context.themePreferences by preferencesDataStore(THEME_PREFERENCES)
object SettingsPreferences {

    enum class ThemeMode(@StringRes val resId: Int) {
        LIGHT(R.string.light_mode),
        DARK(R.string.dark_mode),
        SYSTEM(R.string.follow_system)
    }
    enum class LanguageMode(@StringRes val resId: Int) {
        SIMPLIFIED_CHINESE(R.string.zh_cn),
        TRADITIONAL_CHINESE(R.string.zh_tw),
        ENGLISH(R.string.english),
        SYSTEM(R.string.follow_system)
    }

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE_MODE = stringPreferencesKey("language_mode")

        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    private val themePreferences = App.instance.themePreferences

    val themeMode = themePreferences.getEnum(PreferencesKeys.THEME_MODE, ThemeMode.SYSTEM)
    val languageMode = themePreferences.getEnum(PreferencesKeys.LANGUAGE_MODE, LanguageMode.SYSTEM)

    val firstLaunch = themePreferences.getBoolean(PreferencesKeys.FIRST_LAUNCH, true)
    val dynamicColor = themePreferences.getBoolean(PreferencesKeys.DYNAMIC_COLOR, false)
    private suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
        themePreferences.edit { preferences ->
            preferences[key] = value
        }
    }
    fun applyAppCompatThemeMode(themeMode: ThemeMode) {
        val appCompatMode = when(themeMode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(appCompatMode)
    }

    fun applyAppCompatLanguageMode(languageMode: LanguageMode) {
        val appCompatMode = when(languageMode) {
            LanguageMode.SIMPLIFIED_CHINESE -> LocaleListCompat.forLanguageTags("zh-CN")
            LanguageMode.TRADITIONAL_CHINESE -> LocaleListCompat.forLanguageTags("zh-TW")
            LanguageMode.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            LanguageMode.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(appCompatMode)
    }
    suspend fun changeFirstLaunch(isFirst: Boolean) {
        updatePreference(PreferencesKeys.FIRST_LAUNCH, isFirst)
    }
    suspend fun changeDynamicColor(dynamicTheme: Boolean) {
        updatePreference(PreferencesKeys.DYNAMIC_COLOR, dynamicTheme)
    }
    suspend fun changeThemeMode(themeMode: ThemeMode) {
        withContext(Dispatchers.Main) {
            applyAppCompatThemeMode(themeMode)
        }
        updatePreference(PreferencesKeys.THEME_MODE, themeMode.name)
    }

    suspend fun changeLanguageMode(languageMode: LanguageMode) {
        withContext(Dispatchers.Main) {
            applyAppCompatLanguageMode(languageMode)
        }
        updatePreference(PreferencesKeys.LANGUAGE_MODE, languageMode.name)
    }
}

fun DataStore<Preferences>.getBoolean(key: Preferences.Key<Boolean>, defaultValue: Boolean): Flow<Boolean> {
    return this.data.map { preferences ->
        preferences[key] ?: defaultValue
    }
}