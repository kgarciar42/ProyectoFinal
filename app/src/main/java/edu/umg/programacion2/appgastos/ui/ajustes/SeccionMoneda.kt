package edu.umg.programacion2.appgastos.ui.ajustes

import android.content.DialogInterface
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.datos.Preferencias
import edu.umg.programacion2.appgastos.red.TipoCambio
import edu.umg.programacion2.appgastos.ui.comun.Pantallas
import edu.umg.programacion2.finanzas.servicio.fechaHoy
import kotlinx.coroutines.launch
import java.util.Locale

// Tarjeta Moneda y tipo de cambio: activar dólares, consultar la API o ponerlo a mano
class SeccionMoneda(private val activity: AppCompatActivity, private val prefs: Preferencias) {

    private val tvTasa = activity.findViewById<TextView>(R.id.tvTasa)
    private val tvFecha = activity.findViewById<TextView>(R.id.tvTasaFecha)

    fun configurar() {
        val swDolares = activity.findViewById<CompoundButton>(R.id.swDolares)
        swDolares.isChecked = prefs.usarDolares
        swDolares.setOnCheckedChangeListener { _, activo ->
            prefs.usarDolares = activo
            if (activo) actualizar(forzar = false)
        }
        activity.findViewById<View>(R.id.btnActualizarTasa).setOnClickListener { actualizar(forzar = true) }
        activity.findViewById<View>(R.id.btnTasaManual).setOnClickListener { pedirTasaManual() }
        mostrar()
        actualizar(forzar = false)
    }

    // Consulta la API si no la tengo de hoy
    private fun actualizar(forzar: Boolean) {
        tvFecha.text = "Consultando…"
        activity.lifecycleScope.launch {
            if (TipoCambio.tasaDeHoy(prefs, forzar) == null) {
                tvTasa.text = "1 USD = Q --"
                tvFecha.text = "Sin conexión. Toca Actualizar o ponlo a mano."
            } else {
                mostrar()
            }
        }
    }

    private fun mostrar() {
        if (prefs.tasaDolar <= 0) {
            tvTasa.text = "1 USD = Q --"
            tvFecha.text = "Todavía no se ha consultado"
            return
        }
        tvTasa.text = String.format(Locale.US, "1 USD = Q %.2f", prefs.tasaDolar)
        tvFecha.text = when {
            prefs.tasaManual -> "Puesto a mano el ${prefs.fechaTasa}"
            prefs.fechaTasa == fechaHoy() -> "Actualizado hoy"
            else -> "Último dato: ${prefs.fechaTasa} (sin internet)"
        }
    }

    // Por si no hay internet, lo escribe el usuario
    private fun pedirTasaManual() {
        val (contenedor, campo) = Pantallas.campoParaDialogo(activity, "Ej. 7.68")
        campo.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        campo.gravity = Gravity.CENTER
        if (prefs.tasaDolar > 0) campo.setText(String.format(Locale.US, "%.2f", prefs.tasaDolar))

        val dialogo = AlertDialog.Builder(activity)
            .setTitle("¿Cuántos quetzales vale 1 dólar?")
            .setView(contenedor)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null) // el clic lo manejo abajo para validar
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val tasa = campo.text.toString().trim().replace(',', '.').toDoubleOrNull()
                if (tasa == null || tasa <= 0.0 || tasa > 1000.0) {
                    campo.error = "Escribe un número válido, ej. 7.68"
                } else {
                    prefs.tasaDolar = tasa
                    prefs.fechaTasa = fechaHoy()
                    prefs.tasaManual = true
                    mostrar()
                    dialogo.dismiss()
                }
            }
        }
        dialogo.show()
    }
}