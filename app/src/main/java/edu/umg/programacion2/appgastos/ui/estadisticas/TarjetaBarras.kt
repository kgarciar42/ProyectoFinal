package edu.umg.programacion2.appgastos.ui.estadisticas

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.logica.CalculosEstadisticas
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.appgastos.ui.comun.GraficaBarras
import edu.umg.programacion2.finanzas.servicio.ControlGastos
import edu.umg.programacion2.finanzas.servicio.nombreMes

// Tarjeta 1: barras de ingresos en verde y gastos en rojo de los últimos 6 meses,
// con el mejor y el peor mes.
class TarjetaBarras(activity: AppCompatActivity, private val alTocarMes: (String) -> Unit) {

    private val nombresCortos = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    private val grafica = activity.findViewById<GraficaBarras>(R.id.graficaBarras)
    private val tvResumen = activity.findViewById<TextView>(R.id.tvResumenBarras)
    private var meses: List<String> = emptyList()

    init {
        grafica.alTocarBarra = { indice -> meses.getOrNull(indice)?.let(alTocarMes) }
    }

    suspend fun pintar(mes: String, control: ControlGastos) {
        meses = CalculosEstadisticas.ultimosMeses(mes)
        val balances = meses.map { control.balanceDelMes(it) }
        grafica.indiceResaltado = meses.size - 1
        grafica.mostrar(balances.map {
            GraficaBarras.Barra(
                nombresCortos[it.mes.substring(5, 7).toInt() - 1],
                it.totalIngresos.toFloat(),
                it.totalGastos.toFloat()
            )
        })

        // Mejor y peor mes, solo de los que tienen datos
        val conDatos = balances.filter { it.cantidadIngresos + it.cantidadGastos > 0 }
        val mejor = conDatos.maxByOrNull { it.saldo }
        val peor = conDatos.minByOrNull { it.saldo }
        tvResumen.text = when {
            mejor == null || peor == null -> "Todavía no hay movimientos en estos meses."
            mejor == peor -> "Solo hay datos de ${nombreMes(mejor.mes)}: ${Estilo.dinero(mejor.saldo)}"
            else -> "🏆 Mejor mes: ${nombreMes(mejor.mes)} (${Estilo.dinero(mejor.saldo)})\n" +
                    "📉 Peor mes: ${nombreMes(peor.mes)} (${Estilo.dinero(peor.saldo)})"
        }
    }
}