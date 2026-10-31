package edu.umg.programacion2.appgastos.ui.principal

import android.graphics.drawable.GradientDrawable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.logica.Periodo
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.appgastos.ui.comun.GraficaDona
import edu.umg.programacion2.appgastos.ui.comun.Pantallas
import edu.umg.programacion2.finanzas.modelo.BalanceMensual
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.EstadoBalance
import kotlin.math.roundToInt

// La parte de arriba de la pantalla principal, título del periodo, botón Por mes y Por año,
// tarjeta del balance y gráfica de dona con su leyenda.
// no guarda ni calcula nada.
class VistaResumen(private val activity: AppCompatActivity) {

    private val tvMes = activity.findViewById<TextView>(R.id.tvMes)
    private val btnPorMes = activity.findViewById<TextView>(R.id.btnPorMes)
    private val btnPorAnio = activity.findViewById<TextView>(R.id.btnPorAnio)
    private val cardBalance = activity.findViewById<LinearLayout>(R.id.cardBalance)
    private val tvTituloBalance = activity.findViewById<TextView>(R.id.tvTituloBalance)
    private val tvBalance = activity.findViewById<TextView>(R.id.tvBalance)
    private val tvEstado = activity.findViewById<TextView>(R.id.tvEstado)
    private val tvIngresos = activity.findViewById<TextView>(R.id.tvIngresos)
    private val tvGastos = activity.findViewById<TextView>(R.id.tvGastos)
    private val tvTituloGrafica = activity.findViewById<TextView>(R.id.tvTituloGrafica)
    private val grafica = activity.findViewById<GraficaDona>(R.id.grafica)
    private val tvTotalGrafica = activity.findViewById<TextView>(R.id.tvTotalGrafica)
    private val contenedorLeyenda = activity.findViewById<LinearLayout>(R.id.contenedorLeyenda)

    fun pintar(periodo: Periodo, balance: BalanceMensual) {
        tvMes.text = "${periodo.titulo}  ▾"
        pintarToggle(periodo.porAnio)
        pintarBalance(periodo, balance)
        pintarGrafica(periodo, balance)
    }

    // Pinta de verde la opción activa Por mes o Por año
    private fun pintarToggle(porAnio: Boolean) {
        val acento = ContextCompat.getColor(activity, R.color.acento)
        val fondo = ContextCompat.getColor(activity, R.color.fondo_app)
        val gris = ContextCompat.getColor(activity, R.color.texto_secundario)
        for ((boton, activo) in listOf(btnPorMes to !porAnio, btnPorAnio to porAnio)) {
            boton.background = if (activo) GradientDrawable().apply {
                cornerRadius = Pantallas.dp(activity, 18).toFloat()
                setColor(acento)
            } else null
            boton.setTextColor(if (activo) fondo else gris)
        }
    }

    // La tarjeta grande: saldo, mensaje y totales.
    // Roja si voy mal.
    private fun pintarBalance(periodo: Periodo, balance: BalanceMensual) {
        tvTituloBalance.text = if (periodo.porAnio) "Balance del año" else "Balance del mes"
        tvBalance.text = Estilo.dinero(balance.saldo)
        tvEstado.text = textoEstado(balance)
        tvIngresos.text = "↑ ${Estilo.dinero(balance.totalIngresos)}"
        tvGastos.text = "↓ ${Estilo.dinero(balance.totalGastos)}"
        val vaMal = balance.estado == EstadoBalance.NEGATIVO
        cardBalance.setBackgroundResource(if (vaMal) R.drawable.fondo_balance_negativo else R.drawable.fondo_balance)
        tvEstado.setTextColor(
            when (balance.estado) {
                EstadoBalance.NEGATIVO -> Estilo.ROJO
                EstadoBalance.CUIDADO -> Estilo.NARANJA
                EstadoBalance.POSITIVO -> Estilo.VERDE
                EstadoBalance.SIN_MOVIMIENTOS -> Estilo.GRIS
            }
        )
    }

    // El mensaje de siempre si ya se gastó el 80 % o más, le agrego cuánto le queda disponible
    private fun textoEstado(balance: BalanceMensual): String {
        if (balance.estado != EstadoBalance.CUIDADO) return balance.estado.mensaje
        val libre = 100 - balance.porcentajeGastado
        val disponible = if (libre >= 1) "Te queda el ${libre.roundToInt()} % de tus ingresos para gastar"
        else "Casi no te queda nada de tus ingresos para gastar"
        return "${balance.estado.mensaje}\n$disponible"
    }

    // La dona con las categorías de mayor a menor gasto
    private fun pintarGrafica(periodo: Periodo, balance: BalanceMensual) {
        tvTituloGrafica.text = if (periodo.porAnio) "Gastos del año (${periodo.anio})"
        else "Gastos del mes (${periodo.titulo.substringBefore(" ")})"
        val porCategoria = balance.gastosPorCategoria.entries.sortedByDescending { it.value }
        tvTotalGrafica.text = Estilo.dinero(balance.totalGastos)
        grafica.mostrar(porCategoria.map { it.value.toFloat() to Estilo.colorCategoria(it.key) })
        pintarLeyenda(porCategoria, balance.totalGastos)
    }

    private fun pintarLeyenda(categorias: List<Map.Entry<Categoria, Double>>, total: Double) {
        contenedorLeyenda.removeAllViews()
        if (categorias.isEmpty()) {
            contenedorLeyenda.addView(TextView(activity).apply {
                text = "Sin gastos"
                setTextColor(Estilo.GRIS)
            })
            return
        }
        val radio = Pantallas.dp(activity, 9).toFloat()
        for ((categoria, monto) in categorias) {
            val fila = activity.layoutInflater.inflate(R.layout.item_leyenda, contenedorLeyenda, false)
            val icono = fila.findViewById<TextView>(R.id.tvIconoLeyenda)
            icono.text = Estilo.iconoCategoria(categoria)
            icono.background = Estilo.fondoIcono(Estilo.colorCategoria(categoria), radio)
            fila.findViewById<TextView>(R.id.tvNombreLeyenda).text = categoria.toString()
            val porcentaje = if (total > 0) (monto * 100 / total).roundToInt() else 0
            fila.findViewById<TextView>(R.id.tvPorcentajeLeyenda).text = "$porcentaje%"
            contenedorLeyenda.addView(fila)
        }
    }
}