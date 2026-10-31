package edu.umg.programacion2.finanzas.servicio

import java.util.Calendar

// Aquí tengo todo lo de los meses. Los manejo como texto "yyyy-MM", ej. "2026-10".
// Usé Calendar y no java.time porque java.time no jala en Android viejito (antes de la API 26).

private val FORMATO_FECHA = Regex("""\d{4}-\d{2}-\d{2}""")
private val FORMATO_MES = Regex("""\d{4}-\d{2}""")

private val NOMBRES_MES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

// Reviso que la fecha venga bien, tipo "2026-10-03".
// Si alguien pone mes 13 o día 40, la rechazo.
fun esFechaValida(fecha: String): Boolean {
    if (!FORMATO_FECHA.matches(fecha)) {
        return false
    }
    val mes = fecha.substring(5, 7).toInt()
    val dia = fecha.substring(8, 10).toInt()
    return mes in 1..12 && dia in 1..31
}

// Lo mismo pero solo para el mes, tipo "2026-10".
fun esMesValido(mes: String): Boolean {
    if (!FORMATO_MES.matches(mes)) {
        return false
    }
    return mes.substring(5, 7).toInt() in 1..12
}

// De la fecha saco el mes: "2026-10-03" -> "2026-10".
// Con esto separo un control de otro.
fun mesDe(fecha: String): String = fecha.take(7)

// Armo el mes con dos dígitos para que quede "2026-03" y no "2026-3".
private fun armarMes(anio: Int, mes: Int): String = "$anio-${mes.toString().padStart(2, '0')}"

// El mes en el que estamos. Cuando entra un mes nuevo, esto cambia solito.
fun mesActual(): String {
    val hoy = Calendar.getInstance()
    // Ojo: Calendar cuenta los meses desde 0 (enero = 0), por eso le sumo 1.
    return armarMes(hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH) + 1)
}

// La fecha de hoy completa, ej. "2026-10-23".
fun fechaHoy(): String {
    val hoy = Calendar.getInstance()
    val dia = hoy.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    return "${mesActual()}-$dia"
}

// Para irme al mes de atrás. Si estoy en enero, me regresa a diciembre del año pasado.
fun mesAnterior(mes: String): String {
    var anio = mes.substring(0, 4).toInt()
    var numero = mes.substring(5, 7).toInt() - 1
    if (numero == 0) {
        numero = 12
        anio--
    }
    return armarMes(anio, numero)
}

// Lo mismo pero para adelante. De diciembre paso a enero del otro año.
fun mesSiguiente(mes: String): String {
    var anio = mes.substring(0, 4).toInt()
    var numero = mes.substring(5, 7).toInt() + 1
    if (numero == 13) {
        numero = 1
        anio++
    }
    return armarMes(anio, numero)
}

// Para que se vea bonito en pantalla: "2026-10" -> "Octubre 2026".
fun nombreMes(mes: String): String {
    if (!esMesValido(mes)) {
        return mes
    }
    val numero = mes.substring(5, 7).toInt()
    return "${NOMBRES_MES[numero - 1]} ${mes.substring(0, 4)}"
}

