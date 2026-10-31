package edu.umg.programacion2.appgastos.seguridad

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// Desbloqueo con huella, el bloqueo de pantalla del teléfono PIN, patrón o contraseña.
// y si el teléfono tiene huella, la ofrece primero.
object Biometria {

    private const val TIPOS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    // El teléfono tiene qué verificar huella, un PIN/patrón
    fun huellaDisponible(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(TIPOS) == BiometricManager.BIOMETRIC_SUCCESS

    // Para explicarle al usuario por qué no se puede activar
    fun motivo(context: Context): String = when (BiometricManager.from(context).canAuthenticate(TIPOS)) {
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            "Tu teléfono no tiene huella ni bloqueo de pantalla (PIN, patrón o contraseña) configurado."
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
            "El lector de tu teléfono no está disponible en este momento."
        else -> "Tu teléfono no permite que las apps usen este método."
    }

    // Muestra la ventanita del sistema: huella, el bloqueo del teléfono
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
            .setSubtitle("Usa tu huella o PIN/Patron")
            .setAllowedAuthenticators(TIPOS) // con el bloqueo del teléfono no lleva botón "Usar PIN" propio
            .build()
        prompt.authenticate(info)
    }
}