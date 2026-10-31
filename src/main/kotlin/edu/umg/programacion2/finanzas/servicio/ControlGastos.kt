package edu.umg.programacion2.finanzas.servicio

import edu.umg.programacion2.finanzas.datos.CategoriaRepository
import edu.umg.programacion2.finanzas.datos.CategoriaRepositoryEnMemoria
import edu.umg.programacion2.finanzas.datos.GastoRepository
import edu.umg.programacion2.finanzas.datos.IngresoRepository
import edu.umg.programacion2.finanzas.datos.IngresoRepositoryEnMemoria
import edu.umg.programacion2.finanzas.modelo.BalanceMensual
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.Gasto
import edu.umg.programacion2.finanzas.modelo.Ingreso
import edu.umg.programacion2.finanzas.modelo.TipoIngreso

// Aquí van las reglas validar, registrar, sacar totales y el balance.
// Recibe los repositorios por el constructor y solo conoce las interfaces,
// así puedo cambiar la lista en memoria por SQLite sin tocar nada de aquí.
// solo devuelve datos y la consola o Android deciden cómo mostrarlos.
class ControlGastos(
    private val repositorio: GastoRepository,
    // Valores por defecto por si solo le mando el de gastos (ej. en la consola)
    private val repositorioIngresos: IngresoRepository = IngresoRepositoryEnMemoria(),
    private val repositorioCategorias: CategoriaRepository = CategoriaRepositoryEnMemoria()
) {

    //  GASTOS
    // Si todo está bien devuelve null; si no, devuelve el primer error que encuentre.
    fun validar(gasto: Gasto): String? {
        if (gasto.descripcion.isBlank()) {
            return "La descripción no puede estar vacía"
        }
        if (gasto.monto <= 0) {
            return "El monto debe ser mayor que 0"
        }
        if (!esFechaValida(gasto.fecha)) {
            return "La fecha debe tener el formato yyyy-MM-dd"
        }
        return null
    }

    // Primero valido y si pasa lo guardo. null = se guardó bien.
    suspend fun registrar(gasto: Gasto): String? {
        val error = validar(gasto)
        if (error != null) {
            return error
        }
        repositorio.agregar(gasto)
        return null
    }

    suspend fun listar(): List<Gasto> = repositorio.listar()

    suspend fun eliminar(id: Int): Boolean = repositorio.eliminar(id)

    // Suma de todos los gastos, de todos los meses.
    suspend fun total(): Double {
        var suma = 0.0
        for (gasto in repositorio.listar()) {
            suma += gasto.monto
        }
        return suma
    }

    // Cuánto llevo por categoría de todos los meses. Para un solo mes uso balanceDelMes().
    suspend fun totalPorCategoria(): Map<Categoria, Double> {
        val totales = mutableMapOf<Categoria, Double>()
        for (gasto in repositorio.listar()) {
            val acumulado = totales[gasto.categoria] ?: 0.0
            totales[gasto.categoria] = acumulado + gasto.monto
        }
        return totales
    }

    suspend fun resumen(): String {
        val cantidad = repositorio.listar().size
        val palabra = if (cantidad == 1) "gasto" else "gastos"
        return "$cantidad $palabra · Total: ${formatearQuetzales(total())}"
    }

    // INGRESOS
    // Sin ingresos no hay balance, por eso los agregué salario, ventas, etc.
    // Kotlin me deja tener dos validar() porque reciben cosas distintas.
    fun validar(ingreso: Ingreso): String? {
        if (ingreso.descripcion.isBlank()) {
            return "La descripción no puede estar vacía"
        }
        if (ingreso.monto <= 0) {
            return "El monto debe ser mayor que 0"
        }
        if (!esFechaValida(ingreso.fecha)) {
            return "La fecha debe tener el formato yyyy-MM-dd"
        }
        return null
    }

    suspend fun registrarIngreso(ingreso: Ingreso): String? {
        val error = validar(ingreso)
        if (error != null) {
            return error
        }
        repositorioIngresos.agregar(ingreso)
        return null
    }

    suspend fun listarIngresos(): List<Ingreso> = repositorioIngresos.listar()

    suspend fun eliminarIngreso(id: Int): Boolean = repositorioIngresos.eliminar(id)

    // EDITAR
    // Por si me equivoqué: corrijo un gasto o ingreso que ya guardé. Valido igual que al registrar.
    suspend fun actualizar(gasto: Gasto): String? {
        val error = validar(gasto)
        if (error != null) {
            return error
        }
        if (!repositorio.actualizar(gasto)) {
            return "Ese gasto ya no existe"
        }
        return null
    }

    suspend fun actualizarIngreso(ingreso: Ingreso): String? {
        val error = validar(ingreso)
        if (error != null) {
            return error
        }
        if (!repositorioIngresos.actualizar(ingreso)) {
            return "Ese ingreso ya no existe"
        }
        return null
    }

    // CONTROL POR MES
    // Cada mes es un control aparte
    suspend fun gastosDelMes(mes: String): List<Gasto> =
        repositorio.listar().filter { gasto -> mesDe(gasto.fecha) == mes }

    suspend fun ingresosDelMes(mes: String): List<Ingreso> =
        repositorioIngresos.listar().filter { ingreso -> mesDe(ingreso.fecha) == mes }

    // El balance de un mes.
    // Por eso cada mes empieza de cero: Octubre no se borra, se queda en el historial.
    suspend fun balanceDelMes(mes: String = mesActual()): BalanceMensual =
        armarBalance(mes, gastosDelMes(mes), ingresosDelMes(mes))


    // El mes actual siempre lo meto aunque esté vacío porque es el control que está abierto.
    suspend fun mesesConMovimientos(): List<String> {
        // Uso un Set para que no se repitan: 10 gastos de octubre = "2026-10" una sola vez.
        val meses = mutableSetOf(mesActual())
        for (gasto in repositorio.listar()) {
            meses.add(mesDe(gasto.fecha))
        }
        for (ingreso in repositorioIngresos.listar()) {
            meses.add(mesDe(ingreso.fecha))
        }
        return meses.sortedDescending()
    }

    // El historial: un balance por cada mes.
    suspend fun historial(): List<BalanceMensual> {
        val balances = mutableListOf<BalanceMensual>()
        for (mes in mesesConMovimientos()) {
            balances.add(balanceDelMes(mes))
        }
        return balances
    }

    // Lo que me sobra o me falta sumando todos los meses juntos.
    suspend fun saldoAcumulado(): Double {
        var ingresos = 0.0
        for (ingreso in repositorioIngresos.listar()) {
            ingresos += ingreso.monto
        }
        return ingresos - total()
    }

    // Texto para el encabezado
    suspend fun resumenDelMes(mes: String = mesActual()): String {
        val balance = balanceDelMes(mes)
        return "${nombreMes(mes)} · Ingresos: ${formatearQuetzales(balance.totalIngresos)}" +
                " · Gastos: ${formatearQuetzales(balance.totalGastos)}" +
                " · Balance: ${formatearQuetzales(balance.saldo)}"
    }

    // CONTROL POR AÑO
    // Igual que el control por mes, pero juntando los 12 meses del año.
    // Todo lo de ese año: la fecha empieza con "2026-"
    suspend fun gastosDelAnio(anio: String): List<Gasto> =
        repositorio.listar().filter { gasto -> gasto.fecha.startsWith("$anio-") }

    suspend fun ingresosDelAnio(anio: String): List<Ingreso> =
        repositorioIngresos.listar().filter { ingreso -> ingreso.fecha.startsWith("$anio-") }


    suspend fun balanceDelAnio(anio: String): BalanceMensual =
        armarBalance(anio, gastosDelAnio(anio), ingresosDelAnio(anio))

    // Suma los gastos por categoría y los ingresos por tipo de un periodo y arma el balance.
    // La usan balanceDelMes y balanceDelAnio, así no repito el mismo código dos veces.
    private fun armarBalance(periodo: String, gastos: List<Gasto>, ingresos: List<Ingreso>): BalanceMensual {
        var totalGastos = 0.0
        val gastosPorCategoria = mutableMapOf<Categoria, Double>()
        for (gasto in gastos) {
            totalGastos += gasto.monto
            gastosPorCategoria[gasto.categoria] = (gastosPorCategoria[gasto.categoria] ?: 0.0) + gasto.monto
        }

        var totalIngresos = 0.0
        val ingresosPorTipo = mutableMapOf<TipoIngreso, Double>()
        for (ingreso in ingresos) {
            totalIngresos += ingreso.monto
            ingresosPorTipo[ingreso.tipo] = (ingresosPorTipo[ingreso.tipo] ?: 0.0) + ingreso.monto
        }

        // El saldo y el estado los calcula BalanceMensual
        return BalanceMensual(
            mes = periodo,
            totalIngresos = totalIngresos,
            totalGastos = totalGastos,
            ingresosPorTipo = ingresosPorTipo,
            gastosPorCategoria = gastosPorCategoria,
            cantidadIngresos = ingresos.size,
            cantidadGastos = gastos.size
        )
    }

    // CATEGORÍAS PERSONALIZADAS
    // Puedo crear las categorías que quiera
    suspend fun listarCategorias(): List<Categoria> =
        Categoria.PREDETERMINADAS + repositorioCategorias.listar().sortedBy { it.nombre.lowercase() }

    suspend fun agregarCategoria(nombre: String, icono: String): String? {
        val limpio = nombre.trim()
        if (limpio.isBlank()) {
            return "Escribe un nombre"
        }
        if (limpio.length > 20) {
            return "Máximo 20 letras"
        }
        if (listarCategorias().any { it.nombre.equals(limpio, ignoreCase = true) }) {
            return "Esa categoría ya existe"
        }
        repositorioCategorias.agregar(Categoria(limpio, icono.ifBlank { "🏷️" }))
        return null
    }

    // Borra una categoría mía, sus gastos pasan a Otros para no perderlos.
    suspend fun eliminarCategoria(categoria: Categoria): String? {
        if (categoria.esPredeterminada) {
            return "Las categorías de fábrica no se pueden borrar"
        }
        for (gasto in repositorio.listar()) {
            if (gasto.categoria == categoria) {
                repositorio.actualizar(gasto.copy(categoria = Categoria.OTROS))
            }
        }
        repositorioCategorias.eliminar(categoria.nombre)
        return null
    }
}

// La dejé afuera de la clase para usarla en cualquier lado.
fun formatearQuetzales(monto: Double): String = "Q %.2f".format(monto)