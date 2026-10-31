package edu.umg.programacion2.appgastos.logica

import edu.umg.programacion2.finanzas.servicio.mesActual
import edu.umg.programacion2.finanzas.servicio.mesAnterior
import edu.umg.programacion2.finanzas.servicio.mesDe
import edu.umg.programacion2.finanzas.servicio.mesSiguiente
import edu.umg.programacion2.finanzas.servicio.nombreMes

// Qué estoy viendo en la pantalla principal: un mes o un año completo.
// "mesDeHoy" se puede mandar desde afuera para poder probarla con cualquier fecha.
class Periodo(private val mesDeHoy: String = mesActual()) {

    var porAnio = false
        private set
    var mes = mesDeHoy
        private set
    var anio = mesDeHoy.take(4)
        private set

    // Lo que sale arriba de la pantalla
    val titulo: String
        get() = if (porAnio) "Año $anio" else nombreMes(mes)

    // Las flechas ‹ › mueven un mes o un año, según el modo
    fun anterior() {
        if (porAnio) anio = (anio.toInt() - 1).toString() else verMes(mesAnterior(mes))
    }

    fun siguiente() {
        if (porAnio) anio = (anio.toInt() + 1).toString() else verMes(mesSiguiente(mes))
    }

    fun verMes(nuevoMes: String) {
        porAnio = false
        mes = nuevoMes
        anio = nuevoMes.take(4)
    }

    fun verAnio(nuevoAnio: String) {
        porAnio = true
        anio = nuevoAnio
    }

    // Botón Por mes
    fun cambiarAPorMes() {
        if (porAnio) verMes(if (anio == mesDeHoy.take(4)) mesDeHoy else "$anio-01")
    }

    // Botón Por año
    fun cambiarAPorAnio() {
        if (!porAnio) verAnio(mes.take(4))
    }

    // Botón Inicio, regreso a hoy sin cambiar de modo
    fun irAHoy() {
        mes = mesDeHoy
        anio = mesDeHoy.take(4)
    }

    // Empezar de cero
    fun reiniciar() {
        verMes(mesDeHoy)
    }

    // Después de guardar salto al mes o año de esa fecha
    fun irAFecha(fecha: String) {
        if (porAnio) anio = fecha.take(4) else verMes(mesDe(fecha))
    }

    // Qué mes mando a Estadísticas
    fun mesParaEstadisticas(): String = when {
        !porAnio -> mes
        anio == mesDeHoy.take(4) -> mesDeHoy
        else -> "$anio-12"
    }

    // Con qué fecha arranca un registro nuevo: hoy, si estoy en el periodo actual,
    // si no el primer día del mes o del año que estoy viendo
    // hoy viene como "2026-10-29"
    fun fechaPorDefecto(hoy: String): String = when {
        porAnio && anio == hoy.take(4) -> hoy
        porAnio -> "$anio-01-01"
        mes == hoy.take(7) -> hoy
        else -> "$mes-01"
    }
}