package edu.umg.programacion2.appgastos.ui.ajustes

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.avisos.Notificaciones
import edu.umg.programacion2.appgastos.datos.Preferencias
import edu.umg.programacion2.appgastos.ui.comun.Pantallas

// Todo lo del permiso de notificaciones Android 13+
// y mostrar el aviso rojo con botón para activarlo en el teléfono.
class PermisoNotificaciones(
    private val activity: AppCompatActivity,
    private val prefs: Preferencias,
    private val lanzador: ActivityResultLauncher<String>
) {

    private val tvAviso = activity.findViewById<TextView>(R.id.tvAvisoPermiso)

    init {
        tvAviso.setOnClickListener { abrirAjustesDelTelefono() }
    }

    fun pedirSiHaceFalta() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notificaciones.tienePermiso(activity)) {
            lanzador.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Si dijo que no, le explico y le ofrezco abrir los ajustes del teléfono
    fun alResponder(concedido: Boolean) {
        if (!concedido) {
            AlertDialog.Builder(activity)
                .setTitle("Notificaciones bloqueadas")
                .setMessage("Sin este permiso no puedo mandarte el recordatorio ni las alertas. Puedes activarlo en los ajustes del teléfono.")
                .setNegativeButton("Ahora no", null)
                .setPositiveButton("Abrir ajustes") { _, _ -> abrirAjustesDelTelefono() }
                .show()
        }
        revisar()
    }

    // Aviso rojo si tengo algo de notificaciones prendido pero el teléfono las bloquea
    fun revisar() {
        val necesita = prefs.recordatorioActivo || prefs.alertasPresupuesto || prefs.alertaNegativo
        tvAviso.visibility = if (necesita && !Notificaciones.tienePermiso(activity)) View.VISIBLE else View.GONE
    }

    private fun abrirAjustesDelTelefono() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${activity.packageName}"))
        }
        Pantallas.abrirSeguro(activity, intent)
    }
}