package hf.inner.notebook.page.main

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import hf.inner.notebook.biometric.AppBioMetricManager
import hf.inner.notebook.biometric.BiometricAuthListener
import hf.inner.notebook.page.viewmodel.LocalMemosState
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import hf.inner.notebook.page.viewmodel.LocalTags
import hf.inner.notebook.page.viewmodel.NoteViewModel
import hf.inner.notebook.page.router.App
import hf.inner.notebook.state.NoteState
import hf.inner.notebook.utils.FirstTimeManager
import hf.inner.notebook.utils.SharedPreferencesUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var firstTimeManager: FirstTimeManager

    @Inject
    lateinit var appBioMetricManager: AppBioMetricManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // 自动提取并在屏幕中央显示应用图标
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }
        firstTimeManager.generateIntroduceNoteList()

        lifecycleScope.launch {
            handleAuthentication()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupContent() {
        setContent {
            SettingsProvider {
                App()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun SettingsProvider(
        noteViewModel: NoteViewModel = hiltViewModel(),
        content: @Composable () -> Unit
    ) {
        val state: NoteState by noteViewModel.state.collectAsState(Dispatchers.IO)
        val tags by noteViewModel.tags.collectAsState(Dispatchers.IO)

        CompositionLocalProvider(
            LocalMemosViewModel provides noteViewModel,
            LocalMemosState provides state,
            LocalTags provides tags
        ) {
            content()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun handleAuthentication() {
        val useSafe = SharedPreferencesUtils.useSafe.firstOrNull() ?: false
        if (useSafe && appBioMetricManager.canAuthenticate()) {
            showBiometricPrompt {
                setupContent()
            }
        } else {
            setupContent()
        }
    }

    private fun showBiometricPrompt(success: () -> Unit) {
        appBioMetricManager.initBiometricPrompt(this, object : BiometricAuthListener{
            override fun onBiometricAuthSuccess() {
                success()
            }

            override fun onUserCancelled() {
                finish()
            }

            override fun onErrorOccurred() {
                finish()
            }

        })
    }
}