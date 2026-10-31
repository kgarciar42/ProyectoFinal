package edu.umg.programacion2.appgastos.ui.comun

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

// Gráfica de barras hecha a mano con Canvas, sin librerías extra.
// Por cada mes dibuja dos barras, ingresos en verde y gastos en rojo.
class GraficaBarras @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    data class Barra(val etiqueta: String, val ingresos: Float, val gastos: Float)

    private var barras: List<Barra> = emptyList()

    // Qué barra va resaltada, el mes que estoy viendo y qué hacer al tocar una
    var indiceResaltado = -1
    var alTocarBarra: ((Int) -> Unit)? = null
    private val pincelResaltado = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2DD4BF")
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val pincelIngreso = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#4ADE80") }
    private val pincelGasto = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F87171") }
    private val pincelLinea = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#232B36") }
    private val pincelTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8B98A9")
        textAlign = Paint.Align.CENTER
    }
    private val rect = RectF()

    fun mostrar(nuevas: List<Barra>) {
        barras = nuevas
        invalidate() // que se vuelva a dibujar
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val d = resources.displayMetrics.density
        pincelTexto.textSize = 11 * d
        pincelResaltado.textSize = pincelTexto.textSize
        val abajo = height - 22 * d // dejo espacio abajo para el nombre del mes
        val alto = abajo - 8 * d
        dibujarLineasGuia(canvas, abajo, alto, d)

        if (barras.isEmpty()) return
        val maximo = barras.maxOf { maxOf(it.ingresos, it.gastos) }
        if (maximo <= 0f) {
            canvas.drawText("Sin datos todavía", width / 2f, abajo - alto / 2, pincelTexto)
            return
        }
        val anchoGrupo = width / barras.size.toFloat()
        barras.forEachIndexed { i, barra ->
            dibujarMes(canvas, i, barra, anchoGrupo * i + anchoGrupo / 2, anchoGrupo * 0.26f, abajo, alto / maximo, d)
        }
    }

    // Tres rayitas de fondo como guía 0 %, 50 % y 100 % del valor más alto
    private fun dibujarLineasGuia(canvas: Canvas, abajo: Float, alto: Float, d: Float) {
        for (i in 0..2) {
            val y = abajo - alto * i / 2f
            canvas.drawRect(0f, y - d / 2, width.toFloat(), y + d / 2, pincelLinea)
        }
    }

    // Las dos barras de un mes, ingresos a la izquierda, gastos a la derecha y su nombre abajo
    private fun dibujarMes(canvas: Canvas, i: Int, barra: Barra, centro: Float, ancho: Float, abajo: Float, escala: Float, d: Float) {
        val radio = 4 * d
        val altoIngreso = barra.ingresos * escala
        rect.set(centro - ancho - d, abajo - altoIngreso, centro - d, abajo)
        if (altoIngreso > 0) canvas.drawRoundRect(rect, radio, radio, pincelIngreso)

        val altoGasto = barra.gastos * escala
        rect.set(centro + d, abajo - altoGasto, centro + ancho + d, abajo)
        if (altoGasto > 0) canvas.drawRoundRect(rect, radio, radio, pincelGasto)

        // En verde si es el mes que estoy viendo
        val pincel = if (i == indiceResaltado) pincelResaltado else pincelTexto
        canvas.drawText(barra.etiqueta, centro, height - 6 * d, pincel)
    }

    // Al levantar el dedo veo en qué columna mes, cayó y aviso
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (barras.isEmpty() || alTocarBarra == null) return super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            val indice = (event.x / (width / barras.size.toFloat())).toInt().coerceIn(0, barras.size - 1)
            performClick()
            alTocarBarra?.invoke(indice)
        }
        return true
    }

    override fun performClick(): Boolean = super.performClick()
}