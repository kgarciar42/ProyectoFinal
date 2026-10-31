package edu.umg.programacion2.appgastos.ui.comun

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

// Mi gráfica de dona hecha a mano con Canvas, así no tengo que meter otra librería.
// Le paso una lista de cantidad, color y dibuja cada pedazo del tamaño que le toca.
class GraficaDona @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var datos: List<Pair<Float, Int>> = emptyList()

    private val pincelArco = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    private val pincelTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0B0F14")
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val area = RectF()

    fun mostrar(nuevos: List<Pair<Float, Int>>) {
        datos = nuevos.filter { it.first > 0f }
        invalidate() // le digo a Android que la vuelva a dibujar
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val lado = min(width, height).toFloat()
        val grosor = lado * 0.18f
        val radio = (lado - grosor) / 2f - 2f
        area.set(width / 2f - radio, height / 2f - radio, width / 2f + radio, height / 2f + radio)
        pincelArco.strokeWidth = grosor
        pincelTexto.textSize = grosor * 0.42f

        val total = datos.sumOf { it.first.toDouble() }.toFloat()
        if (total <= 0f) dibujarAroVacio(canvas) else dibujarPedazos(canvas, total, radio)
    }

    // Si no hay gastos dibujo el aro en gris vacío
    private fun dibujarAroVacio(canvas: Canvas) {
        pincelArco.color = Color.parseColor("#252D38")
        canvas.drawArc(area, 0f, 360f, false, pincelArco)
    }

    // Un pedazo por categoría, con una rayita de separación entre pedazos
    private fun dibujarPedazos(canvas: Canvas, total: Float, radio: Float) {
        val separacion = if (datos.size > 1) 2f else 0f
        var inicio = -90f // empiezo arriba, como las agujas del reloj
        for ((valor, color) in datos) {
            val barrido = valor / total * 360f
            pincelArco.color = color
            canvas.drawArc(area, inicio + separacion / 2, (barrido - separacion).coerceAtLeast(0.5f), false, pincelArco)
            dibujarPorcentaje(canvas, (valor / total * 100).roundToInt(), inicio + barrido / 2, radio)
            inicio += barrido
        }
    }

    // El porcentaje en medio del pedazo, solo si es lo bastante grande para que quepa
    private fun dibujarPorcentaje(canvas: Canvas, porcentaje: Int, angulo: Float, radio: Float) {
        if (porcentaje < 7) return
        val rad = Math.toRadians(angulo.toDouble())
        val x = width / 2f + radio * cos(rad).toFloat()
        val y = height / 2f + radio * sin(rad).toFloat() + pincelTexto.textSize / 3
        canvas.drawText("$porcentaje%", x, y, pincelTexto)
    }
}