package edu.umg.programacion2.appgastos.ui.estadisticas

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.logica.CalculosEstadisticas
import edu.umg.programacion2.appgastos.logica.EstadoProyeccion
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.finanzas.modelo.BalanceMensual
import edu.umg.programacion2.finanzas.servicio.mesActual
import edu.umg.programacion2.finanzas.servicio.nombreMes
import java.util.Calendar
import java.util.Locale

// Tarjeta 3: cómo voy a terminar el mes si sigo gastando igual
class TarjetaProyeccion(activity: AppCompatActivity) {

    private val tvValor = activity.findViewById<TextView>(R.id.tvProyeccionValor)
    private val tvTexto = activity.findViewById<TextView>(R.id.tvProyeccionTexto)
    private val tvDetalle = activity.findViewById<TextView>(R.id.tvProyeccionDetalle)
    private val progreso = activity.findViewById<LinearProgressIndicator>(R.id.progresoMes)

    fun pintar(balance: BalanceMensual, mes: String) {
        val hoy = Calendar.getInstance()
        val p = CalculosEstadisticas.proyectar(
            balance, mes, mesActual(),
            dia = hoy.get(Calendar.DAY_OF_MONTH),
            diasDelMes = hoy.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
        val nombre = nombreMes(mes).substringBefore(" ").lowercase(Locale.forLanguageTag("es"))

        progreso.visibility = if (p.estado == EstadoProyeccion.FUTURO) View.GONE else View.VISIBLE
        progreso.progress = p.progresoDelMes
        tvValor.text = if (p.estado == EstadoProyeccion.FUTURO) "--" else Estilo.dinero(p.saldoProyectado)
        tvDetalle.text = when (p.estado) {
            EstadoProyeccion.CERRADO ->
                "Ingresos ${Estilo.dinero(balance.totalIngresos)} · Gastos ${Estilo.dinero(balance.totalGastos)}"
            EstadoProyeccion.FUTURO -> ""
            else -> "Llevas ${Estilo.dinero(balance.totalGastos)} · Promedio ${Estilo.dinero(p.promedioDiario)} " +
                    "por día · Faltan ${p.diasQueFaltan} días"
        }

        val (texto, color) = when (p.estado) {
            EstadoProyeccion.CERRADO -> "${nombreMes(mes)} ya terminó. Así cerró el mes." to
                    (if (p.saldoProyectado < -0.005) Estilo.ROJO else Estilo.VERDE)
            EstadoProyeccion.FUTURO -> "${nombreMes(mes)} todavía no empieza." to Estilo.GRIS
            EstadoProyeccion.SIN_DATOS -> "Registra tus movimientos y te digo cómo vas a terminar el mes." to Estilo.GRIS
            EstadoProyeccion.NEGATIVO -> textoNegativo(nombre, p.maximoPorDia) to Estilo.ROJO
            EstadoProyeccion.JUSTO -> "Vas a terminar en positivo, pero muy justo. Cuida tus gastos." to Estilo.NARANJA
            EstadoProyeccion.BIEN -> "A este ritmo terminarás el mes con ese dinero a tu favor. ¡Vas bien!" to Estilo.VERDE
        }
        tvTexto.text = texto
        tvValor.setTextColor(color)
    }

    // Si voy a terminar en negativo, digo cuánto puedo gastar por día para evitarlo
    private fun textoNegativo(mesEnLetras: String, maximoPorDia: Double?): String {
        val base = "A este ritmo terminarás $mesEnLetras en negativo."
        return if (maximoPorDia != null) "$base Para evitarlo, gasta menos de ${Estilo.dinero(maximoPorDia)} por día." else base
    }
}