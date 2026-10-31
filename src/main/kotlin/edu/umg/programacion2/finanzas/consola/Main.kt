package edu.umg.programacion2.finanzas.consola

import edu.umg.programacion2.finanzas.datos.GastoRepositoryEnMemoria
import edu.umg.programacion2.finanzas.datos.IngresoRepositoryEnMemoria
import edu.umg.programacion2.finanzas.modelo.BalanceMensual
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.EstadoBalance
import edu.umg.programacion2.finanzas.modelo.Gasto
import edu.umg.programacion2.finanzas.modelo.Ingreso
import edu.umg.programacion2.finanzas.modelo.TipoIngreso
import edu.umg.programacion2.finanzas.servicio.ControlGastos
import edu.umg.programacion2.finanzas.servicio.esMesValido
import edu.umg.programacion2.finanzas.servicio.fechaHoy
import edu.umg.programacion2.finanzas.servicio.formatearQuetzales
import edu.umg.programacion2.finanzas.servicio.mesActual
import edu.umg.programacion2.finanzas.servicio.mesAnterior
import edu.umg.programacion2.finanzas.servicio.mesDe
import edu.umg.programacion2.finanzas.servicio.mesSiguiente
import edu.umg.programacion2.finanzas.servicio.nombreMes

//se usa para colores del texto
private const val USAR_COLORES = true
private const val ROJO = "\u001B[31m"
private const val AMARILLO = "\u001B[33m"
private const val VERDE = "\u001B[32m"
private const val NORMAL = "\u001B[0m"

private fun pintar(texto: String, color: String): String =
    if (USAR_COLORES) "$color$texto$NORMAL" else texto



// Le puse suspend al main para poder llamar directo a las funciones suspend del control.
suspend fun main() {
    // 1. Armo el control con sus dos repositorios, uno para gastos y otro para ingresos.
    val control = ControlGastos(GastoRepositoryEnMemoria(), IngresoRepositoryEnMemoria())

    // 2. para meter datos de prueba en dos meses para ver que cada mes va por separado.
    cargarDatosDeEjemplo(control)

    // 3. El menú siempre trabaja sobre un mes, y arranca en el mes en que estamos.
    //    Cuando cambia el mes, mesActual() da el nuevo y el control empieza en Q 0.00.
    var mes = mesActual()

    // 4. El menú se repite hasta que escriba 0.
    while (true) {
        mostrarEncabezado(control.balanceDelMes(mes))
        println("1. Registrar gasto")
        println("2. Registrar ingreso (salario, ventas)")
        println("3. Ver movimientos del mes")
        println("4. Gastos por categoria del mes")
        println("5. Balance detallado del mes")
        println("6. Historial (un control por mes)")
        println("7. Cambiar de mes")
        println("8. Eliminar gasto")
        println("9. Eliminar ingreso")
        println("0. Salir")
        print("Opcion: ")

        when (readlnOrNull()?.trim()) {
            "1" -> registrarGastoDesdeConsola(control)
            "2" -> registrarIngresoDesdeConsola(control)
            "3" -> listarMovimientos(control, mes)
            "4" -> mostrarGastosPorCategoria(control.balanceDelMes(mes))
            "5" -> mostrarBalanceDetallado(control.balanceDelMes(mes))
            "6" -> mostrarHistorial(control)
            "7" -> {
                mes = cambiarMes(mes)
            }
            "8" -> eliminarGastoDesdeConsola(control)
            "9" -> eliminarIngresoDesdeConsola(control)
            "0", null -> {  // null es cuando se cierra la entrada (Ctrl+D)
                println("Hasta pronto")
                return
            }
            else -> println("Opcion no valida")
        }
    }
}

// prueba

private suspend fun cargarDatosDeEjemplo(control: ControlGastos) {
    val esteMes = mesActual()
    val mesPasado = mesAnterior(esteMes)


    control.registrarIngreso(Ingreso(descripcion = "Ventas de la tienda", monto = 800.0, tipo = TipoIngreso.VENTAS, fecha = "$mesPasado-15"))
    control.registrar(Gasto(descripcion = "Inscripcion", monto = 650.0, categoria = Categoria.UNIVERSIDAD, fecha = "$mesPasado-05"))
    control.registrar(Gasto(descripcion = "Supermercado", monto = 300.0, categoria = Categoria.DESPENSA, fecha = "$mesPasado-20"))

    control.registrarIngreso(Ingreso(descripcion = "Salario", monto = 4500.0, tipo = TipoIngreso.SALARIO, fecha = "$esteMes-01"))
    control.registrar(Gasto(descripcion = "Almuerzo", monto = 45.50, categoria = Categoria.COMIDA, fecha = "$esteMes-03"))
    control.registrar(Gasto(descripcion = "Bus a la U", monto = 10.0, categoria = Categoria.TRANSPORTE, fecha = "$esteMes-03"))
}

//muestra balance
private fun mostrarEncabezado(balance: BalanceMensual) {
    val saldo = formatearQuetzales(balance.saldo)
    // Si voy en negativo, el saldo sale en rojo.
    val saldoPintado = if (balance.estado == EstadoBalance.NEGATIVO) pintar(saldo, ROJO) else saldo

    println()
    println("Control de ${nombreMes(balance.mes)} ")
    println(
        "Ingresos: ${formatearQuetzales(balance.totalIngresos)} | " +
                "Gastos: ${formatearQuetzales(balance.totalGastos)} | " +
                "Balance: $saldoPintado"
    )
    println(textoEstado(balance))
}

// mensaje de cómo se encuentra el balance.
private fun textoEstado(balance: BalanceMensual): String {
    val color = when (balance.estado) {
        EstadoBalance.NEGATIVO -> ROJO
        EstadoBalance.CUIDADO -> AMARILLO
        EstadoBalance.POSITIVO -> VERDE
        EstadoBalance.SIN_MOVIMIENTOS -> NORMAL
    }
    return pintar(">> ${balance.estado.mensaje}", color)
}

private fun mostrarBalanceDetallado(balance: BalanceMensual) {
    println("--- Balance de ${nombreMes(balance.mes)} ---")

    println("Ingresos:")
    if (balance.ingresosPorTipo.isEmpty()) {
        println("  (ninguno)")
    }
    for ((tipo, monto) in balance.ingresosPorTipo) {
        println("  + $tipo: ${formatearQuetzales(monto)}")
    }

    println("Gastos:")
    if (balance.gastosPorCategoria.isEmpty()) {
        println("  (ninguno)")
    }
    for ((categoria, monto) in balance.gastosPorCategoria) {
        println("  - $categoria: ${formatearQuetzales(monto)}")
    }

    println("Total ingresos: ${formatearQuetzales(balance.totalIngresos)}")
    println("Total gastos:   ${formatearQuetzales(balance.totalGastos)}")
    println("Balance:        ${formatearQuetzales(balance.saldo)}")
    if (balance.totalIngresos > 0) {
        println("Se ha gastado el ${"%.1f".format(balance.porcentajeGastado)} % de los ingresos")
    }
    println(textoEstado(balance))
}

private fun mostrarGastosPorCategoria(balance: BalanceMensual) {
    if (balance.gastosPorCategoria.isEmpty()) {
        println("(no hay gastos en ${nombreMes(balance.mes)})")
        return
    }
    for ((categoria, monto) in balance.gastosPorCategoria) {
        val porcentaje = monto * 100 / balance.totalGastos
        println("$categoria: ${formatearQuetzales(monto)} (${"%.1f".format(porcentaje)} %)")
    }
}

// Una fila por mes. Aquí se ve que cada mes es un control distinto.
private suspend fun mostrarHistorial(control: ControlGastos) {
    println("Mes".padEnd(18) + "Ingresos".padStart(14) + "Gastos".padStart(14) + "Balance".padStart(14))
    for (balance in control.historial()) {
        val linea = nombreMes(balance.mes).padEnd(18) +
                formatearQuetzales(balance.totalIngresos).padStart(14) +
                formatearQuetzales(balance.totalGastos).padStart(14) +
                formatearQuetzales(balance.saldo).padStart(14)
        if (balance.estado == EstadoBalance.NEGATIVO) {
            println(pintar("$linea  <- vamos mal", ROJO))
        } else {
            println(linea)
        }
    }
    println("Saldo acumulado de todos los meses: ${formatearQuetzales(control.saldoAcumulado())}")
}

// Para moverme de mes. Si escribo algo que no sirve, me quedo en el mismo.
private fun cambiarMes(mesDelMenu: String): String {
    println("Estas viendo ${nombreMes(mesDelMenu)}.")
    print("Escribe yyyy-MM, < (anterior), > (siguiente) o Enter (mes actual): ")
    val texto = readlnOrNull()?.trim() ?: ""
    return when {
        texto.isEmpty() -> mesActual()
        texto == "<" -> mesAnterior(mesDelMenu)
        texto == ">" -> mesSiguiente(mesDelMenu)
        esMesValido(texto) -> texto
        else -> {
            println("ERROR -> Mes no valido, usa el formato yyyy-MM (ej. 2026-09)")
            mesDelMenu
        }
    }
}

// para registrar.
private suspend fun registrarGastoDesdeConsola(control: ControlGastos) {
    print("Descripcion: ")
    val descripcion = readlnOrNull()?.trim() ?: ""

    val monto = leerMonto() ?: return
    val categoria = elegirDeLista(control.listarCategorias()) ?: return
    val fecha = leerFecha()

    val error = control.registrar(Gasto(descripcion = descripcion, monto = monto, categoria = categoria, fecha = fecha))
    if (error == null) {
        println("OK    -> Gasto registrado en el control de ${nombreMes(mesDe(fecha))}")
    } else {
        println("ERROR -> $error")
    }
}

private suspend fun registrarIngresoDesdeConsola(control: ControlGastos) {
    print("Descripcion (ej. Salario de octubre): ")
    val descripcion = readlnOrNull()?.trim() ?: ""

    val monto = leerMonto() ?: return
    val tipo = elegirDeLista(TipoIngreso.entries) ?: return
    val fecha = leerFecha()

    val error = control.registrarIngreso(Ingreso(descripcion = descripcion, monto = monto, tipo = tipo, fecha = fecha))
    if (error == null) {
        println("OK    -> Ingreso registrado en el control de ${nombreMes(mesDe(fecha))}")
    } else {
        println("ERROR -> $error")
    }
}


private fun leerMonto(): Double? {
    print("Monto: ")
    val monto = readlnOrNull()?.trim()?.toDoubleOrNull()
    if (monto == null) {
        println("ERROR -> El monto debe ser un numero")
    }
    return monto
}

// Si dejo la fecha vacía, agarra la de hoy y cae en el control del mes actual.
private fun leerFecha(): String {
    print("Fecha (yyyy-MM-dd, Enter = hoy): ")
    val texto = readlnOrNull()?.trim() ?: ""
    return if (texto.isEmpty()) fechaHoy() else texto
}

// Muestra las opciones con número y me devuelve la que elija.
private fun <T> elegirDeLista(opciones: List<T>): T? {
    for ((indice, opcion) in opciones.withIndex()) {
        println("  ${indice + 1}. $opcion")
    }
    print("Elige una opcion: ")
    val numero = readlnOrNull()?.trim()?.toIntOrNull()
    if (numero == null || numero !in 1..opciones.size) {
        println("ERROR -> Opcion no valida")
        return null
    }
    return opciones[numero - 1]
}

// Listar y eliminar.
private suspend fun listarMovimientos(control: ControlGastos, mes: String) {
    val ingresos = control.ingresosDelMes(mes)
    val gastos = control.gastosDelMes(mes)
    if (ingresos.isEmpty() && gastos.isEmpty()) {
        println("(sin movimientos en ${nombreMes(mes)})")
        return
    }

    println("-- Ingresos --")
    if (ingresos.isEmpty()) {
        println("(ninguno)")
    }
    for (ingreso in ingresos) {
        println("Ingreso #${ingreso.id}  ${ingreso.fecha}  ${ingreso.descripcion} (${ingreso.tipo})  + ${formatearQuetzales(ingreso.monto)}")
    }

    println("-- Gastos --")
    if (gastos.isEmpty()) {
        println("(ninguno)")
    }
    for (gasto in gastos) {
        println("Gasto #${gasto.id}  ${gasto.fecha}  ${gasto.descripcion} (${gasto.categoria})  - ${formatearQuetzales(gasto.monto)}")
    }
}

private suspend fun eliminarGastoDesdeConsola(control: ControlGastos) {
    val id = leerId() ?: return
    if (control.eliminar(id)) {
        println("OK    -> Gasto #$id eliminado")
    } else {
        println("ERROR -> No existe el gasto #$id")
    }
}

private suspend fun eliminarIngresoDesdeConsola(control: ControlGastos) {
    val id = leerId() ?: return
    if (control.eliminarIngreso(id)) {
        println("OK    -> Ingreso #$id eliminado")
    } else {
        println("ERROR -> No existe el ingreso #$id")
    }
}

private fun leerId(): Int? {
    print("Id a eliminar: ")
    val id = readlnOrNull()?.trim()?.toIntOrNull()
    if (id == null) {
        println("ERROR -> El id debe ser un numero")
    }
    return id
}