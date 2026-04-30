package hf.inner.notebook.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import hf.inner.notebook.App
import hf.inner.notebook.page.viewmodel.SortTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SHARED_PREFERENCES_STORE_NAME = "SHARED_PREFERENCES_STORE_NAME"
private val Context.sharedDataStore by preferencesDataStore(SHARED_PREFERENCES_STORE_NAME)

object SharedPreferencesUtils {

    private object PreferencesKeys {
        val SORT_TIME = stringPreferencesKey("sort_time")
        val USE_SAFE = booleanPreferencesKey("use_safe")

        val LOCAL_BACKUP_URI = stringPreferencesKey("local_backup_uri")
        val LOCAL_AUTO_BACKUP = booleanPreferencesKey("local_auto_backup")
        val DAV_SERVER_URL = stringPreferencesKey("dav_server_url")
        val DAV_USER_NAME = stringPreferencesKey("dav_user_name")
        val DAV_PASSWORD = stringPreferencesKey("dav_password")
        val DAV_LOGIN_SUCCESS = booleanPreferencesKey("dav_login_success")

    }

    private val sharedPreferences = App.instance.sharedDataStore

    val sortTime: Flow<SortTime> = sharedPreferences.getEnum(PreferencesKeys.SORT_TIME, SortTime.UPDATE_TIME_DESC)

    val useSafe: Flow<Boolean> = sharedPreferences.getBoolean(PreferencesKeys.USE_SAFE, false)

    val localBackupUri: Flow<String?> = sharedPreferences.getString(PreferencesKeys.LOCAL_BACKUP_URI, null)
    val localAutoBackup: Flow<Boolean> = sharedPreferences.getBoolean(PreferencesKeys.LOCAL_AUTO_BACKUP, false)

    val davServerUrl: Flow<String?> = sharedPreferences.getString(PreferencesKeys.DAV_SERVER_URL, "https://dav.jianguoyun.com/dav/")
    val davUserName: Flow<String?> = sharedPreferences.getString(PreferencesKeys.DAV_USER_NAME, null)
    val davPassword: Flow<String?> = sharedPreferences.getString(PreferencesKeys.DAV_PASSWORD, null)
    val davLoginSuccess: Flow<Boolean>  = sharedPreferences.getBoolean(PreferencesKeys.DAV_LOGIN_SUCCESS, false)

    private suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T?) {
        sharedPreferences.edit { preferences ->
            if (value != null) {
                preferences[key] = value
            } else {
                preferences.remove(key)
            }
        }
    }
    suspend fun updateSortTime(sortTime: SortTime) {
        updatePreference(PreferencesKeys.SORT_TIME, sortTime.name)
    }

    suspend fun updateLocalAutoBackup(use: Boolean) {
        updatePreference(PreferencesKeys.LOCAL_AUTO_BACKUP, use)
    }

    suspend fun updateLocalBackUri(uri: String?){
        updatePreference(PreferencesKeys.LOCAL_BACKUP_URI, uri)
    }

    suspend fun updateDavServerUrl(uri: String) {
        updatePreference(PreferencesKeys.DAV_SERVER_URL, uri)
    }

    suspend fun updateDavUserName(name:String? ) {
        updatePreference(PreferencesKeys.DAV_USER_NAME, name)
    }
    suspend fun updateDavPassword(password:String? ) {
        updatePreference(PreferencesKeys.DAV_PASSWORD, password)
    }
    suspend fun updateDavLoginSuccess(success: Boolean) {
        updatePreference(PreferencesKeys.DAV_LOGIN_SUCCESS, success)
    }

    suspend fun updateUseSafe(use: Boolean) {
        updatePreference(PreferencesKeys.USE_SAFE, use)
    }
    suspend fun clearDavConfig() {
        sharedPreferences.edit { preferences ->
            preferences[PreferencesKeys.DAV_LOGIN_SUCCESS] = false
            preferences.remove(PreferencesKeys.DAV_SERVER_URL)
            preferences.remove(PreferencesKeys.DAV_USER_NAME)
            preferences.remove(PreferencesKeys.DAV_PASSWORD)
        }
    }


}

inline fun <reified T: Enum<T>> DataStore<Preferences>.getEnum(key: Preferences.Key<String>, defaultValue: T): Flow<T> {
    return this.data.map { preferences ->
        preferences[key]?.let {
            try {
                enumValueOf<T>(it)
            } catch (e: IllegalArgumentException) {
                defaultValue
            }
        } ?: defaultValue
    }
}

fun DataStore<Preferences>.getString(key: Preferences.Key<String>, defaultValue: String?): Flow<String?> {
    return this.data.map { preferences ->
        preferences[key] ?: defaultValue
    }
}

fun <T> DataStore<Preferences>.getValue(key: Preferences.Key<T>, defaultValue: T?): Flow<T?> {
    return this.data.map { preferences ->
        (preferences[key] ?: defaultValue)
    }
}

