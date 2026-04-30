package hf.inner.notebook.page.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hf.inner.notebook.biometric.AppBioMetricManager
import hf.inner.notebook.biometric.BiometricAuthListener
import hf.inner.notebook.page.main.MainActivity
import hf.inner.notebook.utils.SharedPreferencesUtils
import hf.inner.notebook.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val appBioMetricManager: AppBioMetricManager,
) : ViewModel() {

    private val _biometricAuthState = MutableStateFlow(false)
    val biometricAuthState: StateFlow<Boolean> = _biometricAuthState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _biometricAuthState.value = SharedPreferencesUtils.useSafe.first()
        }
    }

    fun showBiometricPrompt(activity: MainActivity) {
        appBioMetricManager.initBiometricPrompt(
            activity,
            object : BiometricAuthListener {
                override fun onBiometricAuthSuccess() {
                    viewModelScope.launch {
                        SharedPreferencesUtils.updateUseSafe(!_biometricAuthState.value)
                        _biometricAuthState.value = !_biometricAuthState.value
                    }
                }

                override fun onUserCancelled() {
                }

                override fun onErrorOccurred() {
                }

            }
        )
    }
}