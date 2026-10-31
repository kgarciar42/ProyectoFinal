package edu.umg.programacion2.appgastos.ui.principal

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.appgastos.logica.Periodo
import edu.umg.programacion2.appgastos.ui.comun.Pantallas
import edu.umg.programacion2.finanzas.servicio.mesDe
import kotlinx.coroutines.launch

// Hoja para escoger mes y año. Arriba el año con ‹ › y abajo los 12 meses.
// Los meses que tienen movimientos salen con borde verde para encontrarlos rápido.
// Cambia el Periodo y avisa con alCambiar para que la pantalla se refresque.
class SelectorMes(
    private val activity: AppCompatActivity,
    private val periodo: Periodo,
    private val alCambiar: () -> Unit
) {

    private val nombresCortos = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

    fun mostrar() {
        val (hoja, vista) = Pantallas.crearHoja(activity, R.layout.dialog_mes)
        val tvAnio = vista.findViewById<TextView>(R.id.tvAnio)
        val grid = vista.findViewById<GridLayout>(R.id.gridMeses)
        val btnVerAnio = vista.findViewById<Button>(R.id.btnVerAnio)
        var anioSel = periodo.anio.toInt()

        // Hace el cambio en el periodo, cierra la hoja y avisa para refrescar
        fun elegir(cambio: () -> Unit) {
            cambio()
            hoja.dismiss()
            alCambiar()
        }

        activity.lifecycleScope.launch {
            val conDatos = mesesConMovimientos()
            fun pintarAnio() {
                tvAnio.text = anioSel.toString()
                btnVerAnio.text = "Ver todo el año $anioSel"
                grid.removeAllViews()
                for (i in 1..12) {
                    val clave = "$anioSel-${i.toString().padStart(2, '0')}"
                    val seleccionado = !periodo.porAnio && clave == periodo.mes
                    val celda = crearCelda(nombresCortos[i - 1], seleccionado, clave in conDatos)
                    celda.setOnClickListener { elegir { periodo.verMes(clave) } }
                    grid.addView(celda, parametrosCelda())
                }
            }
            vista.findViewById<View>(R.id.btnAnioAnterior).setOnClickListener { anioSel--; pintarAnio() }
            vista.findViewById<View>(R.id.btnAnioSiguiente).setOnClickListener { anioSel++; pintarAnio() }
            pintarAnio()
        }

        btnVerAnio.setOnClickListener { elegir { periodo.verAnio(anioSel.toString()) } }
        vista.findViewById<View>(R.id.btnMesActual).setOnClickListener { elegir { periodo.reiniciar() } }
        hoja.show()
    }

    // Los meses que tienen algo guardado en gastos o ingresos, tipo "2026-10"
    private suspend fun mesesConMovimientos(): Set<String> {
        val control = Datos.control
        return (control.listar().map { mesDe(it.fecha) } + control.listarIngresos().map { mesDe(it.fecha) }).toSet()
    }

    private fun crearCelda(nombre: String, seleccionado: Boolean, tieneDatos: Boolean): TextView {
        val acento = ContextCompat.getColor(activity, R.color.acento)
        val borde = ContextCompat.getColor(activity, R.color.borde)
        val texto = ContextCompat.getColor(activity, R.color.texto_principal)
        val fondo = ContextCompat.getColor(activity, R.color.fondo_app)
        return TextView(activity).apply {
            text = nombre
            gravity = Gravity.CENTER
            textSize = 15f
            setTypeface(null, if (seleccionado || tieneDatos) Typeface.BOLD else Typeface.NORMAL)
            setTextColor(if (seleccionado) fondo else if (tieneDatos) acento else texto)
            background = GradientDrawable().apply {
                cornerRadius = Pantallas.dp(activity, 12).toFloat()
                setColor(if (seleccionado) acento else Color.TRANSPARENT)
                setStroke(Pantallas.dp(activity, 1.5f), if (seleccionado || tieneDatos) acento else borde)
            }
        }
    }
    private fun parametrosCelda(): GridLayout.LayoutParams {
        val params = GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f))
        params.width = 0
        params.height = Pantallas.dp(activity, 48)
        val margen = Pantallas.dp(activity, 4)
        params.setMargins(margen, margen, margen, margen)
        return params
    }
}