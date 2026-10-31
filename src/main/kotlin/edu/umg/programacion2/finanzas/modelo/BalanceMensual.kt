package edu.umg.programacion2.finanzas.modelo

// El control de un mes: cuánto entró, cuánto salió y cuánto me queda.
// Cada mes es un control nuevo. El mes lo saco de la fecha: "2026-10-03" -> "2026-10".
// El saldo, el porcentaje y el estado no los guardo.
// así nunca se quedan con un valor viejo.
data class BalanceMensual(
    val mes: String,
    val totalIngresos: Double,
    val totalGastos: Double,
    val ingresosPorTipo: Map<TipoIngreso, Double>,
    val gastosPorCategoria: Map<Categoria, Double>,
    val cantidadIngresos: Int,
    val cantidadGastos: Int
) {
    // Lo que me queda. Si sale negativo es porque gasté más de lo que me entró.
    val saldo: Double
        get() = totalIngresos - totalGastos

    // Qué porcentaje de lo que me entró ya me gasté. Si no hay ingresos, 0.
    val porcentajeGastado: Double
        get() = if (totalIngresos > 0) totalGastos * 100 / totalIngresos else 0.0

    val estado: EstadoBalance
        get() = when {
            cantidadIngresos == 0 && cantidadGastos == 0 -> EstadoBalance.SIN_MOVIMIENTOS
            saldo < -0.005 -> EstadoBalance.NEGATIVO
            totalIngresos > 0 && porcentajeGastado >= 80 -> EstadoBalance.CUIDADO
            else -> EstadoBalance.POSITIVO
        }
}
