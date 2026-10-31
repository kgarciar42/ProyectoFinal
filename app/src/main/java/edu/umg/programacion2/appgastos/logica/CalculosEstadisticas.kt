package edu.umg.programacion2.appgastos.logica

import edu.umg.programacion2.finanzas.modelo.BalanceMensual
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.servicio.mesAnterior
import kotlin.math.abs
import kotlin.math.roundToInt

// Resultado de comparar un mes con el anterior.
data class Comparacion(
    val porcentaje: Int?,
    val gastoActual: Double,
    val gastoAnterior: Double,
    val cambiosPorCategoria: List<Pair<Categoria, Double>>
)

enum class EstadoProyeccion { CERRADO, FUTURO, SIN_DATOS, NEGATIVO, JUSTO, BIEN }

// Cómo voy a terminar el mes si sigo gastando igual
data class Proyeccion(
    val estado: EstadoProyeccion,
    val saldoProyectado: Double,
    val promedioDiario: Double,
    val diasQueFaltan: Int,
    val maximoPorDia: Double?, // cuánto puedo gastar por día para no quedar en negativo
    val progresoDelMes: Int
)

// Las cuentas de la pantalla de Estadísticas, probada en CalculosEstadisticasTest.
object CalculosEstadisticas {

    fun ultimosMeses(mes: String, cantidad: Int = 6): List<String> {
        val meses = mutableListOf<String>()
        var actual = mes
        repeat(cantidad) {
            meses.add(0, actual)
            actual = mesAnterior(actual)
        }
        return meses
    }

    fun comparar(actual: BalanceMensual, anterior: BalanceMensual): Comparacion {
        val porcentaje = if (anterior.totalGastos > 0) {
            ((actual.totalGastos - anterior.totalGastos) / anterior.totalGastos * 100).roundToInt()
        } else {
            null
        }
        // Las categorías que más cambiaron máximo 5, de mayor a menor
        val cambios = (actual.gastosPorCategoria.keys + anterior.gastosPorCategoria.keys)
            .map { it to (actual.gastosPorCategoria[it] ?: 0.0) - (anterior.gastosPorCategoria[it] ?: 0.0) }
            .filter { abs(it.second) >= 0.01 }
            .sortedByDescending { abs(it.second) }
            .take(5)
        return Comparacion(porcentaje, actual.totalGastos, anterior.totalGastos, cambios)
    }

    fun proyectar(balance: BalanceMensual, mes: String, mesDeHoy: String, dia: Int, diasDelMes: Int): Proyeccion {
        if (mes < mesDeHoy) return Proyeccion(EstadoProyeccion.CERRADO, balance.saldo, 0.0, 0, null, 100)
        if (mes > mesDeHoy) return Proyeccion(EstadoProyeccion.FUTURO, 0.0, 0.0, diasDelMes, null, 0)

        val promedio = balance.totalGastos / dia
        val gastoProyectado = promedio * diasDelMes
        val saldoProyectado = balance.totalIngresos - gastoProyectado
        val faltan = diasDelMes - dia
        val disponible = balance.totalIngresos - balance.totalGastos
        val estado = when {
            balance.cantidadGastos == 0 && balance.cantidadIngresos == 0 -> EstadoProyeccion.SIN_DATOS
            saldoProyectado < -0.005 -> EstadoProyeccion.NEGATIVO
            gastoProyectado >= balance.totalIngresos * 0.8 -> EstadoProyeccion.JUSTO
            else -> EstadoProyeccion.BIEN
        }
        val maximo = if (estado == EstadoProyeccion.NEGATIVO && faltan > 0 && disponible > 0) disponible / faltan else null
        return Proyeccion(estado, saldoProyectado, promedio, faltan, maximo, dia * 100 / diasDelMes)
    }
}