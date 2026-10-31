package edu.umg.programacion2.appgastos.ui.principal

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.appgastos.ui.comun.Pantallas
import edu.umg.programacion2.finanzas.modelo.Gasto
import edu.umg.programacion2.finanzas.modelo.Ingreso

// La lista de movimientos ingresos y gastos juntos, los más recientes arriba.
class ListaMovimientos(
    private val activity: AppCompatActivity,
    private val alTocar: (Any) -> Unit,
    private val alMantener: (Any) -> Unit
) {

    private data class Fila(
        val original: Any,
        val id: Int,
        val esIngreso: Boolean,
        val icono: String,
        val color: Int,
        val titulo: String,
        val detalle: String,
        val monto: Double,
        val fecha: String
    )

    private val tvTitulo = activity.findViewById<TextView>(R.id.tvTituloMovimientos)
    private val contenedor = activity.findViewById<LinearLayout>(R.id.contenedorMovimientos)
    private val tvVacio = activity.findViewById<TextView>(R.id.tvSinMovimientos)

    fun pintar(porAnio: Boolean, ingresos: List<Ingreso>, gastos: List<Gasto>) {
        tvTitulo.text = if (porAnio) "Movimientos del año" else "Movimientos del mes"
        tvVacio.text = if (porAnio) "Este año no tiene movimientos. Toca + para registrar."
        else "Mes nuevo, control nuevo. Toca + para registrar."

        val filas = (ingresos.map { aFila(it) } + gastos.map { aFila(it) })
            .sortedWith(compareByDescending<Fila> { it.fecha }.thenByDescending { it.id })
        contenedor.removeAllViews()
        tvVacio.visibility = if (filas.isEmpty()) View.VISIBLE else View.GONE
        filas.forEach { contenedor.addView(crearVista(it)) }
    }

    private fun aFila(i: Ingreso) = Fila(
        i, i.id, true, Estilo.iconoIngreso(i.tipo), Estilo.VERDE, i.descripcion, i.tipo.toString(), i.monto, i.fecha
    )

    private fun aFila(g: Gasto) = Fila(
        g, g.id, false, Estilo.iconoCategoria(g.categoria), Estilo.colorCategoria(g.categoria),
        g.descripcion, g.categoria.toString(), g.monto, g.fecha
    )

    private fun crearVista(f: Fila): View {
        val vista = activity.layoutInflater.inflate(R.layout.item_movimiento, contenedor, false)
        val icono = vista.findViewById<TextView>(R.id.tvIconoMov)
        icono.text = f.icono
        icono.background = Estilo.fondoIcono(f.color, Pantallas.dp(activity, 12).toFloat())
        vista.findViewById<TextView>(R.id.tvTituloMov).text = f.titulo
        vista.findViewById<TextView>(R.id.tvDetalleMov).text = f.detalle
        vista.findViewById<TextView>(R.id.tvFechaMov).text = Estilo.fechaCorta(f.fecha)
        val tvMonto = vista.findViewById<TextView>(R.id.tvMontoMov)
        tvMonto.text = (if (f.esIngreso) "+" else "-") + Estilo.dinero(f.monto)
        tvMonto.setTextColor(if (f.esIngreso) Estilo.VERDE else Estilo.ROJO)

        vista.setOnClickListener { alTocar(f.original) }
        vista.setOnLongClickListener {
            alMantener(f.original)
            true
        }
        return vista
    }
}