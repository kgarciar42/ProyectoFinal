package edu.umg.programacion2.appgastos.ui.principal

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.finanzas.modelo.Gasto
import edu.umg.programacion2.finanzas.modelo.Ingreso
import kotlinx.coroutines.launch

// Pregunta antes de borrar un gasto o ingreso.
// alEliminado recibe la fecha del que se borró para refrescar y revisar si el mes quedó en negativo.
object DialogoEliminar {

    fun confirmar(activity: AppCompatActivity, original: Any, alEliminado: (fecha: String) -> Unit) {
        val (titulo, monto, fecha) = when (original) {
            is Gasto -> Triple(original.descripcion, original.monto, original.fecha)
            is Ingreso -> Triple(original.descripcion, original.monto, original.fecha)
            else -> return
        }
        AlertDialog.Builder(activity)
            .setTitle("Eliminar")
            .setMessage("¿Eliminar \"$titulo\" por ${Estilo.dinero(monto)}?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                activity.lifecycleScope.launch {
                    if (original is Gasto) Datos.control.eliminar(original.id)
                    if (original is Ingreso) Datos.control.eliminarIngreso(original.id)
                    Toast.makeText(activity, "Eliminado", Toast.LENGTH_SHORT).show()
                    alEliminado(fecha)
                }
            }
            .show()
    }
}