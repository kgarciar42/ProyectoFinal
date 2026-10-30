package edu.umg.programacion2.appgastos.seguridad

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// Todo lo de huella o rostro
object Biometria {

    private const val TIPO = BiometricManager.Authenticators.BIOMETRIC_WEAK

    // Si el teléfono tiene huella o rostro configurado?
    fun huellaDisponible(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(TIPO) == BiometricManager.BIOMETRIC_SUCCESS

    // Muestra la ventana del sistema para poner la huella o el rostro
    fun pedirHuella(activity: FragmentActivity, alExito: () -> Unit) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    alExito()
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear la app")
            .setSubtitle("Usa tu huella o tu rostro")
            .setNegativeButtonText("Usar PIN")
            .setAllowedAuthenticators(TIPO)
            .build()
        prompt.authenticate(info)
    }
}