package edu.umg.programacion2.appgastos.avisos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import edu.umg.programacion2.appgastos.datos.Preferencias


// 1. A la hora del recordatorio manda la notificación y programa el del dia siguiente
// 2. Cuando el teléfono se prende BOOT_COMPLETED vuelve a programar el recordatorio,
//    porque Android borra todas las alarmas al apagarse
class RecordatorioReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = Preferencias(context)
        if (!prefs.recordatorioActivo) return

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            Notificaciones.mostrar(
                context,
                Notificaciones.CANAL_RECORDATORIO,
                2001,
                "¿Ya registraste tus gastos de hoy?",
                "Toma un minuto para anotar lo que gastaste hoy y mantener tu control al día."
            )
        }
        Notificaciones.programarRecordatorio(context, prefs.horaRecordatorio, prefs.minutoRecordatorio)
    }
}