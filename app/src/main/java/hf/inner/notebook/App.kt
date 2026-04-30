package hf.inner.notebook

import android.app.Application
import android.util.Log
import androidx.lifecycle.asLiveData
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import hf.inner.notebook.backup.BackupScheduler
import hf.inner.notebook.db.repo.TagNoteRepo
import hf.inner.notebook.page.settings.SettingsPreferenceScreen
import hf.inner.notebook.utils.SettingsPreferences
import hf.inner.notebook.utils.SharedPreferencesUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

fun getAppName(): String {
    return "IdeaMemo"
}

@HiltAndroidApp
class App : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    override fun onCreate() {
        super.onCreate()
        instance = this

        val localAutoBackup = SharedPreferencesUtils.localAutoBackup.asLiveData().value
        if (localAutoBackup == true) {
            BackupScheduler.scheduleDailyBackup(this)
        } else {
            BackupScheduler.cancelDailyBackup(this)
        }

        applicationScope.launch {
            val currentTheme =SettingsPreferences.themeMode.first()
            SettingsPreferences.applyAppCompatThemeMode(currentTheme)

            val currentLanguage = SettingsPreferences.languageMode.first()
            SettingsPreferences.applyAppCompatLanguageMode(currentLanguage)
        }
    }

    companion object {
        lateinit var instance: App
    }
}

@InstallIn(SingletonComponent::class)
@EntryPoint
interface AppEntryPoint {
    fun tagNoteRepo(): TagNoteRepo
}