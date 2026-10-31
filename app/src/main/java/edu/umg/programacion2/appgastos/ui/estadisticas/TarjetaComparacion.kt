package edu.umg.programacion2.appgastos.ui.estadisticas

import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.logica.CalculosEstadisticas
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.appgastos.ui.comun.Pantallas
import edu.umg.programacion2.finanzas.modelo.BalanceMensual
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.servicio.mesAnterior
import edu.umg.programacion2.finanzas.servicio.nombreMes
import kotlin.math.abs

// Tarjeta 2: este mes contra el anterior, el total y las categorías que más cambiaron
class TarjetaComparacion(private val activity: AppCompatActivity) {

    private val tvTitulo = activity.findViewById<TextView>(R.id.tvComparacionTitulo)
    private val tvValor = activity.findViewById<TextView>(R.id.tvComparacionValor)
    private val tvDetalle = activity.findViewById<TextView>(R.id.tvComparacionDetalle)
    private val contenedor = activity.findViewById<LinearLayout>(R.id.contenedorComparacion)

    fun pintar(actual: BalanceMensual, anterior: BalanceMensual, mes: String) {
        val c = CalculosEstadisticas.comparar(actual, anterior)
        tvTitulo.text = "${nombreMes(mes)} vs ${nombreMes(mesAnterior(mes))}"
        tvDetalle.text = "Este mes: ${Estilo.dinero(c.gastoActual)} · Mes anterior: ${Estilo.dinero(c.gastoAnterior)}"

        // Subió = rojo gasté más, bajó = verde gasté menos
        val porcentaje = c.porcentaje
        val (texto, color) = when {
            porcentaje == null && c.gastoActual <= 0 -> "Sin gastos para comparar" to Estilo.GRIS
            porcentaje == null -> "El mes anterior no tuvo gastos" to Estilo.GRIS
            porcentaje > 0 -> "▲ $porcentaje % más gasto" to Estilo.ROJO
            porcentaje < 0 -> "▼ ${abs(porcentaje)} % menos gasto" to Estilo.VERDE
            else -> "Gastaste lo mismo" to Estilo.GRIS
        }
        tvValor.text = texto
        tvValor.setTextColor(color)

        contenedor.removeAllViews()
        c.cambiosPorCategoria.forEach { (categoria, diferencia) -> contenedor.addView(crearFila(categoria, diferencia)) }
    }

    // 🍔 Comida
    private fun crearFila(categoria: Categoria, diferencia: Double): LinearLayout {
        val fila = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val p = Pantallas.dp(activity, 6)
            setPadding(0, p, 0, p)
        }
        val nombre = TextView(activity).apply {
            text = "${Estilo.iconoCategoria(categoria)}  $categoria"
            setTextColor(ContextCompat.getColor(activity, R.color.texto_principal))
            textSize = 14f
        }
        val monto = TextView(activity).apply {
            text = (if (diferencia > 0) "▲ +" else "▼ -") + Estilo.dinero(abs(diferencia))
            setTextColor(if (diferencia > 0) Estilo.ROJO else Estilo.VERDE)
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
        }
        fila.addView(nombre, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        fila.addView(monto)
        return fila
    }
}