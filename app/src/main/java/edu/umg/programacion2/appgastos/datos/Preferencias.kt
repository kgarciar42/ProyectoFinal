package edu.umg.programacion2.appgastos.datos

import android.content.Context
import java.security.MessageDigest

// Aquí guardo todos los ajustes en el teléfono (SharedPreferences).
// Se quedan guardados aunque cierre la app.
class Preferencias(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("ajustes_app", Context.MODE_PRIVATE)

    // MONEDA
    var usarDolares: Boolean
        get() = prefs.getBoolean("usar_dolares", false)
        set(valor) = prefs.edit().putBoolean("usar_dolares", valor).apply()

    // Cuántos quetzales vale 1 dólar (la última vez que lo consulté)
    var tasaDolar: Double
        get() = prefs.getString("tasa_dolar", null)?.toDoubleOrNull() ?: 0.0
        set(valor) = prefs.edit().putString("tasa_dolar", valor.toString()).apply()

    // El día en que consulté la tasa, tipo "2026-10-29"
    var fechaTasa: String
        get() = prefs.getString("fecha_tasa", "") ?: ""
        set(valor) = prefs.edit().putString("fecha_tasa", valor).apply()

    // si el tipo de cambio lo escribí a mano (si no hay internet)
    var tasaManual: Boolean
        get() = prefs.getBoolean("tasa_manual", false)
        set(valor) = prefs.edit().putBoolean("tasa_manual", valor).apply()

    // SEGURIDAD
    val tienePin: Boolean
        get() = prefs.getString("pin_hash", null) != null

    // El PIN no lo guardo tal cual, así nadie lo puede leer
    fun guardarPin(pin: String) = prefs.edit().putString("pin_hash", hash(pin)).apply()

    fun quitarPin() = prefs.edit()
        .remove("pin_hash")
        .remove("recuperacion_hash")
        .remove("pregunta_recuperacion")
        .putBoolean("usar_huella", false)
        .apply()

    fun pinCorrecto(pin: String): Boolean = prefs.getString("pin_hash", null) == hash(pin)

    // Pregunta secreta para recuperar el PIN si se me olvida.
    // La respuesta también la guardo cifrada y sin importar mayúsculas ni espacios.
    val tieneRecuperacion: Boolean
        get() = prefs.getString("recuperacion_hash", null) != null

    val preguntaRecuperacion: String
        get() = prefs.getString("pregunta_recuperacion", "") ?: ""

    fun guardarRecuperacion(pregunta: String, respuesta: String) = prefs.edit()
        .putString("pregunta_recuperacion", pregunta.trim())
        .putString("recuperacion_hash", hash(respuesta.trim().lowercase()))
        .apply()

    fun respuestaCorrecta(respuesta: String): Boolean =
        prefs.getString("recuperacion_hash", null) == hash(respuesta.trim().lowercase())

    var usarHuella: Boolean
        get() = prefs.getBoolean("usar_huella", false)
        set(valor) = prefs.edit().putBoolean("usar_huella", valor).apply()

    var ocultarSaldos: Boolean
        get() = prefs.getBoolean("ocultar_saldos", false)
        set(valor) = prefs.edit().putBoolean("ocultar_saldos", valor).apply()

    // Borra todos los ajustes se usa solo en empezar de cero
    fun borrarTodo() = prefs.edit().clear().apply()

    // NOTIFICACIONES
    var recordatorioActivo: Boolean
        get() = prefs.getBoolean("recordatorio_activo", false)
        set(valor) = prefs.edit().putBoolean("recordatorio_activo", valor).apply()

    var horaRecordatorio: Int
        get() = prefs.getInt("hora_recordatorio", 20) // 8:00 PM por defecto
        set(valor) = prefs.edit().putInt("hora_recordatorio", valor).apply()

    var minutoRecordatorio: Int
        get() = prefs.getInt("minuto_recordatorio", 0)
        set(valor) = prefs.edit().putInt("minuto_recordatorio", valor).apply()

    var alertasPresupuesto: Boolean
        get() = prefs.getBoolean("alertas_presupuesto", false)
        set(valor) = prefs.edit().putBoolean("alertas_presupuesto", valor).apply()

    // Notificación cuando el balance queda negativo encendida por defecto
    var alertaNegativo: Boolean
        get() = prefs.getBoolean("alerta_negativo", true)
        set(valor) = prefs.edit().putBoolean("alerta_negativo", valor).apply()

    // Para pedir el permiso de notificaciones solo una vez al abrir la app
    var yaPidioPermiso: Boolean
        get() = prefs.getBoolean("ya_pidio_permiso", false)
        set(valor) = prefs.edit().putBoolean("ya_pidio_permiso", valor).apply()

    // Límite mensual de una categoría, 0 = sin límite.
    // La clave va en minúsculas, también leo MAYÚSCULAS
    fun presupuesto(categoria: String): Double {
        val texto = prefs.getString("presupuesto_${categoria.lowercase()}", null)
            ?: prefs.getString("presupuesto_${categoria.uppercase()}", null)
        return texto?.toDoubleOrNull() ?: 0.0
    }

    fun guardarPresupuesto(categoria: String, monto: Double) {
        val editor = prefs.edit().remove("presupuesto_${categoria.uppercase()}") // quito la clave vieja
        if (monto > 0) {
            editor.putString("presupuesto_${categoria.lowercase()}", monto.toString())
        } else {
            editor.remove("presupuesto_${categoria.lowercase()}")
        }
        editor.apply()
    }

    // Para no mandar la misma notificación mil veces, anoto qué el aviso ya lo mandé ese mes
    fun yaAvise(clave: String): Boolean = prefs.getBoolean("aviso_$clave", false)

    fun marcarAviso(clave: String) = prefs.edit().putBoolean("aviso_$clave", true).apply()

    fun quitarAviso(clave: String) = prefs.edit().remove("aviso_$clave").apply()

    private fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256").digest(texto.toByteArray()).joinToString("") { "%02x".format(it) }
}