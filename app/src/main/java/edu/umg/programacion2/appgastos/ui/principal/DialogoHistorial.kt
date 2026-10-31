package edu.umg.programacion2.appgastos.ui.principal

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.finanzas.modelo.EstadoBalance
import edu.umg.programacion2.finanzas.servicio.nombreMes
import kotlinx.coroutines.launch

// El balance de cada mes cada mes es un control aparte.
// Al tocar un mes lo abre.
object DialogoHistorial {

    fun mostrar(activity: AppCompatActivity, alElegirMes: (mes: String) -> Unit) {
        activity.lifecycleScope.launch {
            val balances = Datos.control.historial()
            val renglones = balances.map { b ->
                val marca = if (b.estado == EstadoBalance.NEGATIVO) "  ⚠️" else ""
                "${nombreMes(b.mes)}:  ${Estilo.dinero(b.saldo)}$marca"
            }.toTypedArray()
            AlertDialog.Builder(activity)
                .setTitle("Historial · toca un mes para verlo")
                .setItems(renglones) { _, posicion -> alElegirMes(balances[posicion].mes) }
                .setPositiveButton("Cerrar", null)
                .show()
        }
    }
}