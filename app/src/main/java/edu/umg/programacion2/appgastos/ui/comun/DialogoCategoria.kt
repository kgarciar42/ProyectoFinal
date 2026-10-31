package edu.umg.programacion2.appgastos.ui.comun

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.finanzas.modelo.Categoria
import kotlinx.coroutines.launch

// La ventanita para crear una categoría nueva, nombre + escoger un ícono.
object DialogoCategoria {

    private val ICONOS = listOf("🏷️", "🏠", "💡", "📱", "🎁", "👕", "🐶", "⛽", "📚", "🎉", "✈️", "💪")

    fun mostrar(activity: AppCompatActivity, alCrear: (Categoria) -> Unit) {
        val etNombre = EditText(activity).apply {
            hint = "Nombre (ej. Mascotas, Gimnasio)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            filters = arrayOf(InputFilter.LengthFilter(20))
        }
        var iconoElegido = ICONOS[0]
        val selector = crearSelectorIconos(activity) { iconoElegido = it }

        val dialogo = AlertDialog.Builder(activity)
            .setTitle("Nueva categoría")
            .setView(armarFormulario(activity, etNombre, selector))
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Crear", null) // el clic lo manejo abajo para validar
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                crear(activity, etNombre, iconoElegido) { nueva ->
                    dialogo.dismiss()
                    alCrear(nueva)
                }
            }
        }
        dialogo.show()
    }

    // La librería valida: que no esté vacía, máximo 20 letras y que no se repita
    private fun crear(activity: AppCompatActivity, etNombre: EditText, icono: String, alCrear: (Categoria) -> Unit) {
        val nombre = etNombre.text.toString().trim()
        activity.lifecycleScope.launch {
            val error = Datos.control.agregarCategoria(nombre, icono)
            if (error != null) {
                etNombre.error = error
                return@launch
            }
            Toast.makeText(activity, "Categoría \"$nombre\" creada", Toast.LENGTH_SHORT).show()
            alCrear(Categoria(nombre, icono))
        }
    }

    private fun armarFormulario(activity: AppCompatActivity, etNombre: EditText, selector: GridLayout) =
        LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            val p = Pantallas.dp(activity, 20)
            setPadding(p, Pantallas.dp(activity, 8), p, 0)
            addView(etNombre)
            addView(TextView(activity).apply {
                text = "Escoge un ícono:"
                setPadding(0, Pantallas.dp(activity, 14), 0, Pantallas.dp(activity, 4))
            })
            addView(selector)
        }

    // Cuadrícula de íconos, el icono escogido lleva un cuadrito verde detrás
    private fun crearSelectorIconos(activity: AppCompatActivity, alElegir: (String) -> Unit): GridLayout {
        val grid = GridLayout(activity).apply { columnCount = 6 }
        val celdas = ICONOS.map { icono ->
            TextView(activity).apply {
                text = icono
                tag = icono
                textSize = 22f
                gravity = Gravity.CENTER
            }
        }
        fun marcar(elegido: String) {
            celdas.forEach { it.background = if (it.tag == elegido) fondoElegido(activity) else null }
            alElegir(elegido)
        }
        for (celda in celdas) {
            celda.setOnClickListener { marcar(celda.tag as String) }
            val params = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f))
            params.width = 0
            params.height = Pantallas.dp(activity, 46)
            grid.addView(celda, params)
        }
        marcar(ICONOS[0])
        return grid
    }

    private fun fondoElegido(activity: AppCompatActivity) = GradientDrawable().apply {
        cornerRadius = Pantallas.dp(activity, 10).toFloat()
        setColor(Color.parseColor("#332DD4BF"))
        setStroke(Pantallas.dp(activity, 2), Color.parseColor("#2DD4BF"))
    }
}