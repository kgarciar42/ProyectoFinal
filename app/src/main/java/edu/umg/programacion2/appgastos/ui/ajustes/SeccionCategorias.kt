package edu.umg.programacion2.appgastos.ui.ajustes

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.appgastos.datos.Preferencias
import edu.umg.programacion2.appgastos.ui.comun.DialogoCategoria
import edu.umg.programacion2.appgastos.ui.comun.Pantallas
import edu.umg.programacion2.finanzas.modelo.Categoria
import kotlinx.coroutines.launch

// Tarjeta Mis categorías: ver, crear y borrar las categorías que yo creé.
// alCambiar avisa para que los límites de presupuesto también se actualicen.
class SeccionCategorias(
    private val activity: AppCompatActivity,
    private val prefs: Preferencias,
    private val alCambiar: () -> Unit
) {

    private val contenedor = activity.findViewById<LinearLayout>(R.id.contenedorMisCategorias)

    fun configurar() {
        activity.findViewById<View>(R.id.btnNuevaCategoria).setOnClickListener {
            DialogoCategoria.mostrar(activity) {
                pintar()
                alCambiar()
            }
        }
        pintar()
    }

    private fun pintar() {
        activity.lifecycleScope.launch {
            val mias = Datos.control.listarCategorias().filter { !it.esPredeterminada }
            contenedor.removeAllViews()
            if (mias.isEmpty()) {
                contenedor.addView(texto("Todavía no has creado categorías.", R.color.texto_secundario))
                return@launch
            }
            mias.forEach { contenedor.addView(crearFila(it)) }
        }
    }

    // "🐶 Mascotas                    🗑️"
    private fun crearFila(categoria: Categoria): LinearLayout {
        val fila = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val p = Pantallas.dp(activity, 6)
            setPadding(0, p, 0, p)
        }
        val borrar = TextView(activity).apply {
            text = "🗑️"
            textSize = 20f
            val p = Pantallas.dp(activity, 8)
            setPadding(p, p, p, p)
            setOnClickListener { confirmarBorrar(categoria) }
        }
        val nombre = texto("${categoria.icono}  ${categoria.nombre}", R.color.texto_principal)
        fila.addView(nombre, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        fila.addView(borrar)
        return fila
    }

    private fun texto(contenido: String, color: Int) = TextView(activity).apply {
        text = contenido
        textSize = 15f
        setTextColor(ContextCompat.getColor(activity, color))
        setPadding(0, Pantallas.dp(activity, 6), 0, 0)
    }

    // Sus gastos pasan a Otros eso lo hace la librería en eliminarCategoria
    private fun confirmarBorrar(categoria: Categoria) {
        AlertDialog.Builder(activity)
            .setTitle("Borrar ${categoria.nombre}")
            .setMessage("Los gastos que tenga esta categoría pasarán a \"Otros\" para que no se pierdan.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Borrar") { _, _ ->
                activity.lifecycleScope.launch {
                    val error = Datos.control.eliminarCategoria(categoria)
                    if (error != null) {
                        Toast.makeText(activity, error, Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    prefs.guardarPresupuesto(categoria.nombre, 0.0) // quito su límite
                    Toast.makeText(activity, "Categoría borrada", Toast.LENGTH_SHORT).show()
                    pintar()
                    alCambiar()
                }
            }
            .show()
    }
}