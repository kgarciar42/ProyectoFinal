package edu.umg.programacion2.appgastos.logica

import java.util.Locale

// Todo lo de pasar dólares a quetzales, probada en ConversionDolaresTest.
object ConversionDolares {

    // 100 dólares a 7.68 son Q 768.00
    fun aQuetzales(dolares: Double, tasa: Double): Double = Math.round(dolares * tasa * 100) / 100.0

    fun conNota(descripcion: String, dolares: Double): String =
        if (descripcion.isEmpty()) descripcion
        else descripcion + String.format(Locale.US, " (US$ %.2f)", dolares)

    fun sinNota(descripcion: String): String =
        descripcion.replace(Regex("""\s*\(US\$ [0-9.,]+\)$"""), "")

    // Lee lo que escribió el usuario, acepta coma o punto, Si no es 0.
    fun leerMonto(texto: String): Double = texto.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
}