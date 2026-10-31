package edu.umg.programacion2.appgastos.ui.ajustes

import android.content.Intent
import android.provider.Settings
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.datos.Preferencias
import edu.umg.programacion2.appgastos.seguridad.Biometria
import edu.umg.programacion2.appgastos.ui.comun.Pantallas

// Tarjeta Seguridad y privacidad: PIN, huella o bloqueo del teléfono, y ocultar saldos
class SeccionSeguridad(private val activity: AppCompatActivity, private val prefs: Preferencias) {

    private val swPin = activity.findViewById<CompoundButton>(R.id.swPin)
    private val swHuella = activity.findViewById<CompoundButton>(R.id.swHuella)
    private val swOcultar = activity.findViewById<CompoundButton>(R.id.swOcultar)

    fun configurar() {
        swPin.isChecked = prefs.tienePin
        swHuella.isChecked = prefs.usarHuella
        swHuella.isEnabled = prefs.tienePin // la huella necesita el PIN
        swOcultar.isChecked = prefs.ocultarSaldos

        swPin.setOnCheckedChangeListener { boton, activo -> cambiarPin(boton, activo) }
        swHuella.setOnCheckedChangeListener { boton, activo -> cambiarHuella(boton, activo) }
        swOcultar.setOnCheckedChangeListener { _, activo -> prefs.ocultarSaldos = activo }
    }

    private fun cambiarPin(boton: CompoundButton, activo: Boolean) {
        if (activo && !prefs.tienePin) {
            // Si cancela, regreso el interruptor a apagado
            DialogoCrearPin(activity, prefs,
                alGuardar = { swHuella.isEnabled = true },
                alCancelar = { boton.isChecked = false }
            ).mostrar()
        } else if (!activo && prefs.tienePin) {
            prefs.quitarPin()
            swHuella.isChecked = false
            swHuella.isEnabled = false
            Toast.makeText(activity, "PIN desactivado", Toast.LENGTH_SHORT).show()
        }
    }

    // Si el teléfono no tiene con qué verificar, le explico por qué y le ofrezco ir a Ajustes
    private fun cambiarHuella(boton: CompoundButton, activo: Boolean) {
        if (activo && !Biometria.huellaDisponible(activity)) {
            boton.isChecked = false
            AlertDialog.Builder(activity)
                .setTitle("No se puede activar")
                .setMessage(Biometria.motivo(activity) + "\n\nRegistra tu huella o pon un bloqueo de pantalla en " +
                        "Ajustes del teléfono → Seguridad, y luego vuelve a activar esta opción.")
                .setNegativeButton("Ahora no", null)
                .setPositiveButton("Ir a ajustes") { _, _ -> abrirAjustesSeguridad() }
                .show()
        } else {
            prefs.usarHuella = activo
            if (activo) Toast.makeText(activity, "Listo: podrás entrar con tu huella o el bloqueo del teléfono", Toast.LENGTH_LONG).show()
        }
    }

    // Abre la parte de Seguridad de los ajustes del teléfono funciona igual en Samsung y en otros
    private fun abrirAjustesSeguridad() {
        Pantallas.abrirSeguro(activity, Intent(Settings.ACTION_SECURITY_SETTINGS))
    }
}