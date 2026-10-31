package edu.umg.programacion2.appgastos.ui.ajustes

import android.app.TimePickerDialog
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.avisos.Notificaciones
import edu.umg.programacion2.appgastos.datos.Preferencias
import java.util.Locale

// Tarjeta Notificaciones y recordatorios: recordatorio diario con su hora,
// alerta de balance negativo, alertas de presupuesto y notificación de prueba
class SeccionNotificaciones(
    private val activity: AppCompatActivity,
    private val prefs: Preferencias,
    private val permiso: PermisoNotificaciones
) {

    private val tvHora = activity.findViewById<TextView>(R.id.tvHora)

    fun configurar() {
        configurarRecordatorio()
        interruptor(R.id.swNegativo, prefs.alertaNegativo) { prefs.alertaNegativo = it }
        interruptor(R.id.swAlertas, prefs.alertasPresupuesto) { prefs.alertasPresupuesto = it }
        activity.findViewById<View>(R.id.btnProbarNotificacion).setOnClickListener { probar() }
        permiso.revisar()
    }

    // Para los interruptores de alertas: guardar, pedir permiso si se prende y revisar el aviso rojo
    private fun interruptor(id: Int, valorInicial: Boolean, guardar: (Boolean) -> Unit) {
        val sw = activity.findViewById<CompoundButton>(id)
        sw.isChecked = valorInicial
        sw.setOnCheckedChangeListener { _, activo ->
            guardar(activo)
            if (activo) permiso.pedirSiHaceFalta()
            permiso.revisar()
        }
    }

    private fun configurarRecordatorio() {
        val sw = activity.findViewById<CompoundButton>(R.id.swRecordatorio)
        sw.isChecked = prefs.recordatorioActivo
        pintarHora()
        sw.setOnCheckedChangeListener { _, activo ->
            prefs.recordatorioActivo = activo
            permiso.revisar()
            if (activo) {
                permiso.pedirSiHaceFalta()
                Notificaciones.programarRecordatorio(activity, prefs.horaRecordatorio, prefs.minutoRecordatorio)
                Toast.makeText(activity, "Te recordaré todos los días", Toast.LENGTH_SHORT).show()
            } else {
                Notificaciones.cancelarRecordatorio(activity)
            }
        }
        // Tocar la hora abre el reloj para cambiarla
        tvHora.setOnClickListener {
            TimePickerDialog(activity, { _, hora, minuto ->
                prefs.horaRecordatorio = hora
                prefs.minutoRecordatorio = minuto
                pintarHora()
                if (prefs.recordatorioActivo) Notificaciones.programarRecordatorio(activity, hora, minuto)
            }, prefs.horaRecordatorio, prefs.minutoRecordatorio, true).show()
        }
    }

    private fun pintarHora() {
        val hora = String.format(Locale.US, "%02d:%02d", prefs.horaRecordatorio, prefs.minutoRecordatorio)
        tvHora.text = "⏰  Todos los días a las $hora"
    }

    // Para comprobar que sí llegan
    private fun probar() {
        if (!Notificaciones.tienePermiso(activity)) {
            permiso.pedirSiHaceFalta()
            return
        }
        Notificaciones.mostrar(
            activity, Notificaciones.CANAL_RECORDATORIO, ID_PRUEBA,
            "Notificación de prueba", "¡Funciona! Así te van a llegar los avisos de la app."
        )
    }

    private companion object {
        const val ID_PRUEBA = 2999
    }
}