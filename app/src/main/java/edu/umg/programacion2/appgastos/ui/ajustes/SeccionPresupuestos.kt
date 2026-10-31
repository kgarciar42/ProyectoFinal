package edu.umg.programacion2.appgastos.ui.ajustes

import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.appgastos.datos.Preferencias
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.appgastos.ui.comun.Pantallas
import edu.umg.programacion2.finanzas.modelo.Categoria
import kotlinx.coroutines.launch

// Límite mensual por categoría
// Lo usa AvisosFinancieros para avisar al llegar al 80 % y al 100 %.
class SeccionPresupuestos(private val activity: AppCompatActivity, private val prefs: Preferencias) {

    private val contenedor = activity.findViewById<LinearLayout>(R.id.contenedorPresupuestos)

    // Se vuelve a llamar cuando creo o borro una categoría
    fun pintar() {
        activity.lifecycleScope.launch {
            val campos = mutableMapOf<Categoria, EditText>()
            contenedor.removeAllViews()
            for (categoria in Datos.control.listarCategorias()) {
                val campo = crearCampo(categoria)
                contenedor.addView(crearFila(categoria, campo))
                campos[categoria] = campo
            }
            activity.findViewById<View>(R.id.btnGuardarPresupuestos).setOnClickListener { guardar(campos) }
        }
    }

    private fun guardar(campos: Map<Categoria, EditText>) {
        for ((categoria, campo) in campos) {
            val monto = campo.text.toString().trim().replace(',', '.').toDoubleOrNull() ?: 0.0
            prefs.guardarPresupuesto(categoria.nombre, monto)
        }
        Toast.makeText(activity, "Límites guardados", Toast.LENGTH_SHORT).show()
    }

    // 🛒 Despensa
    private fun crearFila(categoria: Categoria, campo: EditText): LinearLayout {
        val fila = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, Pantallas.dp(activity, 8), 0, 0)
        }
        val nombre = TextView(activity).apply {
            text = "${Estilo.iconoCategoria(categoria)}  $categoria"
            setTextColor(ContextCompat.getColor(activity, R.color.texto_principal))
            textSize = 15f
        }
        fila.addView(nombre, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        fila.addView(campo, LinearLayout.LayoutParams(Pantallas.dp(activity, 130), LinearLayout.LayoutParams.WRAP_CONTENT))
        return fila
    }

    private fun crearCampo(categoria: Categoria) = EditText(activity).apply {
        hint = "Sin límite"
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        setBackgroundResource(R.drawable.fondo_campo)
        setTextColor(ContextCompat.getColor(activity, R.color.texto_principal))
        setHintTextColor(ContextCompat.getColor(activity, R.color.texto_secundario))
        val p = Pantallas.dp(activity, 12)
        setPadding(p, p, p, p)
        gravity = Gravity.END
        val limite = prefs.presupuesto(categoria.nombre)
        if (limite > 0) setText(Estilo.montoParaEditar(limite))
    }
}