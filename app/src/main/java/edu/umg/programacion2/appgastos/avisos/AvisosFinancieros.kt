package edu.umg.programacion2.appgastos.avisos

import android.content.Context
import android.widget.Toast
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.appgastos.datos.Preferencias
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.EstadoBalance
import edu.umg.programacion2.finanzas.servicio.nombreMes
import java.util.Locale
import kotlin.math.roundToInt

// Los avisos de dinero que salen después de guardar o borrar algo
// 1. Alerta de presupuesto 80 % y 100 % del límite de una categoría
// 2. Notificación de ¡Vamos mal! cuando el mes queda en negativo
class AvisosFinancieros(private val context: Context, private val prefs: Preferencias) {

    private val control get() = Datos.control

    suspend fun revisarPresupuesto(categoria: Categoria, mes: String) {
        if (!prefs.alertasPresupuesto) return
        val limite = prefs.presupuesto(categoria.nombre)
        if (limite <= 0) return

        val gastado = control.gastosDelMes(mes).filter { it.categoria == categoria }.sumOf { it.monto }
        val porcentaje = (gastado * 100 / limite).roundToInt()
        val (nivel, titulo) = when {
            porcentaje >= 100 -> "100" to "Te pasaste del límite de $categoria"
            porcentaje >= 80 -> "80" to "Has gastado el $porcentaje% en $categoria"
            else -> return
        }
        Toast.makeText(context, titulo, Toast.LENGTH_LONG).show()

        // La notificación solo una vez por mes, categoría y nivel, para no llenar el celular
        val clave = "${mes}_${categoria.nombre.lowercase()}_$nivel"
        if (prefs.yaAvise(clave)) return
        prefs.marcarAviso(clave)
        val texto = String.format(Locale.US, "Llevas Q %,.2f de Q %,.2f este mes.", gastado, limite)
        val id = ID_PRESUPUESTO + (categoria.nombre.lowercase().hashCode() and 0x3FF)
        Notificaciones.mostrar(context, Notificaciones.CANAL_ALERTAS, id, titulo, texto)
    }

    // Avisa una sola vez, si el mes se recupera y vuelve a caer, avisa otra vez
    suspend fun revisarBalanceNegativo(mes: String) {
        val balance = control.balanceDelMes(mes)
        val clave = "negativo_$mes"
        if (balance.estado != EstadoBalance.NEGATIVO) {
            prefs.quitarAviso(clave)
            return
        }
        if (!prefs.alertaNegativo || prefs.yaAvise(clave)) return
        prefs.marcarAviso(clave)
        Notificaciones.mostrar(
            context, Notificaciones.CANAL_ALERTAS, ID_NEGATIVO, "⚠️ ¡Vamos mal!",
            "Tu balance de ${nombreMes(mes)} quedó en ${Estilo.dinero(balance.saldo)}. Estás gastando más de lo que ingresa."
        )
    }

    private companion object {
        const val ID_PRESUPUESTO = 3000
        const val ID_NEGATIVO = 4000
    }
}