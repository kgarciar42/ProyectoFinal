package edu.umg.programacion2.appgastos.red

import edu.umg.programacion2.appgastos.datos.Preferencias
import edu.umg.programacion2.finanzas.servicio.fechaHoy
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

// Consulta el tipo de cambio de dólar a quetzales con una API REST gratis.
object TipoCambio {

    private const val URL_API = "https://open.er-api.com/v6/latest/USD"

    // Devuelve cuántos quetzales cuesta 1 dólar, o null si no hay internet.
    // Va en Dispatchers.IO porque Android no deja usar internet en el hilo de la pantalla.
    suspend fun consultar(): Double? = withContext<Double?>(Dispatchers.IO) {
        try {
            val conexion = URL(URL_API).openConnection() as HttpURLConnection
            conexion.connectTimeout = 8000
            conexion.readTimeout = 8000
            try {
                if (conexion.responseCode != 200) return@withContext null
                val texto = conexion.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(texto)
                if (json.optString("result") != "success") return@withContext null
                json.getJSONObject("rates").getDouble("GTQ")
            } finally {
                conexion.disconnect()
            }
        } catch (e: Exception) {
            null
        }
    }

    // Si ya la consulté hoy uso la que habia guardado, si no, le pido a la API REST y la guardo.
    // Sin internet uso la última que tenga.
    suspend fun tasaDeHoy(prefs: Preferencias, forzar: Boolean = false): Double? {
        val hoy = fechaHoy()
        if (!forzar && prefs.fechaTasa == hoy && prefs.tasaDolar > 0) return prefs.tasaDolar
        val nueva = consultar()
        if (nueva != null) {
            prefs.tasaDolar = nueva
            prefs.fechaTasa = hoy
            prefs.tasaManual = false // ya vino de internet
            return nueva
        }
        return if (prefs.tasaDolar > 0) prefs.tasaDolar else null
    }
}