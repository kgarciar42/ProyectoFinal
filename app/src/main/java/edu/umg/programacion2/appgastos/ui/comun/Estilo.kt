package edu.umg.programacion2.appgastos.ui.comun

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.TipoIngreso
import java.util.Locale
import kotlin.math.abs

// todo lo visual
//  colores, íconos emojis y el formato del dinero.
object Estilo {

    var ocultarSaldos = false

    val VERDE = Color.parseColor("#4ADE80")
    val ROJO = Color.parseColor("#F87171")
    val NARANJA = Color.parseColor("#FB923C")
    val GRIS = Color.parseColor("#8B98A9")

    private val COLORES_FABRICA = mapOf(
        "despensa" to "#34D399",
        "comida" to "#3B82F6",
        "universidad" to "#A855F7",
        "transporte" to "#FB923C",
        "salud" to "#F472B6",
        "entretenimiento" to "#FACC15",
        "otros" to "#22D3EE"
    )

    // Para las categorías que crea el usuario escojo uno de estos según su nombre
    // siempre le toca el mismo color a la misma categoría
    private val COLORES_EXTRA = listOf("#F97316", "#14B8A6", "#E879F9", "#84CC16", "#60A5FA", "#FB7185", "#C084FC", "#FBBF24")

    fun colorCategoria(categoria: Categoria): Int {
        val clave = categoria.nombre.lowercase()
        val hex = COLORES_FABRICA[clave] ?: COLORES_EXTRA[(clave.hashCode() and 0x7FFFFFFF) % COLORES_EXTRA.size]
        return Color.parseColor(hex)
    }

    // El ícono ya viene dentro de la categoría, las que trae predeterminadamente y las que yo creo
    fun iconoCategoria(categoria: Categoria): String = categoria.icono

    fun iconoIngreso(tipo: TipoIngreso): String = when (tipo.name) {
        "SALARIO" -> "💼"
        "VENTAS" -> "🏷️"
        else -> "💰"
    }

    fun fondoIcono(color: Int, radio: Float): GradientDrawable = GradientDrawable().apply {
        cornerRadius = radio
        setColor(Color.argb(55, Color.red(color), Color.green(color), Color.blue(color)))
    }

    fun dinero(monto: Double): String {
        if (ocultarSaldos) return "Q ****.**"
        val texto = "Q " + String.format(Locale.US, "%,.2f", abs(monto))
        return if (monto < -0.005) "-$texto" else texto
    }

    // de esto "2026-10-23" paso a esto "23/10/2026"
    fun fechaLarga(fecha: String): String =
        "${fecha.substring(8, 10)}/${fecha.substring(5, 7)}/${fecha.substring(0, 4)}"

    // para que al editar no salga "1500.0"
    fun montoParaEditar(monto: Double): String =
        if (monto % 1.0 == 0.0) monto.toLong().toString() else monto.toString()

    // de esto "2026-10-23" paso a esto "23/10"
    fun fechaCorta(fecha: String): String =
        if (fecha.length >= 10) "${fecha.substring(8, 10)}/${fecha.substring(5, 7)}" else fecha
}