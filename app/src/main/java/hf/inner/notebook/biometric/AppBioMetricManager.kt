package hf.inner.notebook.biometric

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import dagger.hilt.android.qualifiers.ApplicationContext
import hf.inner.notebook.R
import hf.inner.notebook.page.main.MainActivity
import javax.inject.Inject

class AppBioMetricManager @Inject constructor(@ApplicationContext appContext: Context) {
    private var biometricPrompt: BiometricPrompt? = null

    private val biometricManager = BiometricManager.from(appContext)

    /**
     * 是否支持生物识别
     */
    fun canAuthenticate(): Boolean {
        // BiometricManager.Authenticators.BIOMETRIC_STRONG : 是否支持强安全级别
        val combinedFlags = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val result = biometricManager.canAuthenticate(combinedFlags)
        return when(result) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                val singleResult = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                singleResult == BiometricManager.BIOMETRIC_SUCCESS
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else -> false

        }
    }

    fun initBiometricPrompt(activity: MainActivity, listener: BiometricAuthListener) {
        biometricPrompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    val cancelled = errorCode in arrayListOf(
                        BiometricPrompt.ERROR_CANCELED,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    )
                    if (cancelled) {
                        listener.onUserCancelled()
                    } else {
                        listener.onErrorOccurred()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    listener.onBiometricAuthSuccess()
                }
            }
        )
//        val promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
//            .setTitle(activity.getString(R.string.biometric_authentication))
//            .setNegativeButtonText(activity.getString(R.string.cancel))
//            .build()
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_authentication))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } else {
            // 29 开启密码验证
            builder.setDeviceCredentialAllowed(true)
        }
        val promptInfo = builder.build()
        biometricPrompt?.authenticate(promptInfo)
    }
}