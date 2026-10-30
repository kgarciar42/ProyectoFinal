package edu.umg.programacion2.appgastos.avisos

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import edu.umg.programacion2.appgastos.ui.principal.MainActivity
import java.util.Calendar

// Todo lo de notificaciones, el recordatorio diario y las alertas de presupuesto
object Notificaciones {

    const val CANAL_RECORDATORIO = "recordatorio_diario"
    const val CANAL_ALERTAS = "alertas_presupuesto"

    // Android 8+ pide canales antes de mandar notificaciones
    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CANAL_RECORDATORIO, "Recordatorio diario", NotificationManager.IMPORTANCE_DEFAULT)
            )
            manager.createNotificationChannel(
                NotificationChannel(CANAL_ALERTAS, "Alertas de presupuesto", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    // Android 13+ pide permiso para notificar, en los anteriores ya viene permitido
    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // el permiso lo reviso arriba con tienePermiso()
    fun mostrar(context: Context, canal: String, id: Int, titulo: String, texto: String) {
        if (!tienePermiso(context)) return
        crearCanales(context)
        val abrirApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notificacion)
    }

    // Programa el próximo recordatorio, el Receiver programa el del día siguiente.
    // sin pedir permisos especiales de alarma exacta.
    fun programarRecordatorio(context: Context, hora: Int, minuto: Int) {
        val alarma = context.getSystemService(AlarmManager::class.java)
        val cuando = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Si esa hora ya pasó hoy, arranca mañana
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarma.cancel(intentRecordatorio(context)) // quito el anterior para no tener dos
        alarma.setWindow(AlarmManager.RTC_WAKEUP, cuando.timeInMillis, 10 * 60 * 1000L, intentRecordatorio(context))
    }

    fun cancelarRecordatorio(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(intentRecordatorio(context))
    }

    private fun intentRecordatorio(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, 100, Intent(context, RecordatorioReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}