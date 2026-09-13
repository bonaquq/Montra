package com.example.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthManager {

    fun getBiometricStatus(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SecurityUpdateRequired
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricStatus.Unsupported
            else -> BiometricStatus.Unknown
        }
    }

    fun isBiometricSupported(context: Context): Boolean {
        val status = getBiometricStatus(context)
        return status == BiometricStatus.Available || status == BiometricStatus.NotEnrolled
    }

    fun canAuthenticateNow(context: Context): Boolean {
        return getBiometricStatus(context) == BiometricStatus.Available
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock Montra",
        subtitle: String = "Scan fingerprint or face recognition to continue",
        description: String? = null,
        negativeButtonText: String = "Use PIN / Password",
        allowDeviceCredential: Boolean = false,
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: String) -> Unit,
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        try {
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)

            if (!description.isNullOrEmpty()) {
                promptInfoBuilder.setDescription(description)
            }

            if (allowDeviceCredential) {
                promptInfoBuilder.setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
            } else {
                promptInfoBuilder.setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                promptInfoBuilder.setNegativeButtonText(negativeButtonText)
            }

            biometricPrompt.authenticate(promptInfoBuilder.build())
        } catch (e: Exception) {
            onError(-1, e.localizedMessage ?: "Failed to start biometric authentication")
        }
    }
}

fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

sealed class BiometricStatus(val message: String) {
    object Available : BiometricStatus("Biometrics available and enrolled")
    object NoHardware : BiometricStatus("No biometric sensor detected on this device")
    object HardwareUnavailable : BiometricStatus("Biometric sensor is currently unavailable")
    object NotEnrolled : BiometricStatus("No biometric data enrolled in device security settings")
    object SecurityUpdateRequired : BiometricStatus("Security update required for biometric sensor")
    object Unsupported : BiometricStatus("Biometrics unsupported on this device")
    object Unknown : BiometricStatus("Biometric status unknown")
}
