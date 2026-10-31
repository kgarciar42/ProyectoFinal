package edu.umg.programacion2.appgastos.ui.principal

import android.app.DatePickerDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.appgastos.datos.Preferencias
import edu.umg.programacion2.appgastos.logica.ConversionDolares
import edu.umg.programacion2.appgastos.logica.GuardadoMovimiento
import edu.umg.programacion2.appgastos.ui.comun.DialogoCategoria
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.appgastos.ui.comun.Pantallas
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.Gasto
import edu.umg.programacion2.finanzas.modelo.Ingreso
import edu.umg.programacion2.finanzas.modelo.TipoIngreso
import kotlinx.coroutines.launch
import java.util.Locale

// La hoja que sube desde abajo para registrar o editar un gasto/ingreso.
// Solo maneja la parte visual, la decisión de qué guardar está en GuardadoMovimiento.
class HojaRegistro(
    private val activity: AppCompatActivity,
    private val prefs: Preferencias,
    private val alGuardar: (fecha: String, categoria: Categoria?, esEdicion: Boolean) -> Unit,
    private val alEliminar: (fecha: String) -> Unit
) {

    private class Campos(vista: View) {
        val titulo: TextView = vista.findViewById(R.id.tvTituloHoja)
        val rgTipo: RadioGroup = vista.findViewById(R.id.rgTipo)
        val rbIngreso: RadioButton = vista.findViewById(R.id.rbIngreso)
        val spOpcion: Spinner = vista.findViewById(R.id.spOpcion)
        val tvNuevaCategoria: TextView = vista.findViewById(R.id.tvNuevaCategoria)
        val etDescripcion: EditText = vista.findViewById(R.id.etDescripcion)
        val etMonto: EditText = vista.findViewById(R.id.etMonto)
        val tvFecha: TextView = vista.findViewById(R.id.tvFecha)
        val grupoMoneda: View = vista.findViewById(R.id.grupoMoneda)
        val rgMoneda: RadioGroup = vista.findViewById(R.id.rgMoneda)
        val tvConversion: TextView = vista.findViewById(R.id.tvConversion)
        val btnGuardar: Button = vista.findViewById(R.id.btnGuardar)
        val btnEliminar: Button = vista.findViewById(R.id.btnEliminar)
        var fecha = ""
        var categorias: List<Categoria> = emptyList()
    }

    // editando = null para uno nuevo
    // el Gasto/Ingreso que voy a editar
    fun abrir(editando: Any?, fechaInicial: String, categorias: List<Categoria>) {
        val (hoja, vista) = Pantallas.crearHoja(activity, R.layout.dialog_registrar)
        val c = Campos(vista)
        c.categorias = categorias
        c.fecha = when (editando) {
            is Gasto -> editando.fecha
            is Ingreso -> editando.fecha
            else -> fechaInicial
        }
        configurarTipo(c)
        configurarMoneda(c)
        configurarFecha(c)
        if (editando != null) llenarParaEditar(c, editando, hoja) else cargarSpinner(c)
        c.btnGuardar.setOnClickListener { guardar(c, editando, hoja) }
        hoja.show()
    }

    private fun configurarTipo(c: Campos) {
        c.rgTipo.setOnCheckedChangeListener { _, _ -> cargarSpinner(c) }
        c.tvNuevaCategoria.setOnClickListener {
            DialogoCategoria.mostrar(activity) { nueva ->
                activity.lifecycleScope.launch {
                    c.categorias = Datos.control.listarCategorias()
                    cargarSpinner(c)
                    c.spOpcion.setSelection(c.categorias.indexOf(nueva).coerceAtLeast(0)) // queda escogida
                }
            }
        }
    }

    private fun cargarSpinner(c: Campos) {
        val esIngreso = c.rgTipo.checkedRadioButtonId == R.id.rbIngreso
        val opciones: List<Any> = if (esIngreso) TipoIngreso.entries else c.categorias
        c.spOpcion.adapter = ArrayAdapter(activity, R.layout.item_spinner, opciones).apply {
            setDropDownViewResource(R.layout.item_spinner)
        }
        c.tvNuevaCategoria.visibility = if (esIngreso) View.GONE else View.VISIBLE
    }

    // Quetzales/Dólares, solo si lo activé en Ajustes con la conversión
    private fun configurarMoneda(c: Campos) {
        c.grupoMoneda.visibility = if (prefs.usarDolares) View.VISIBLE else View.GONE
        c.rgMoneda.setOnCheckedChangeListener { _, _ -> pintarConversion(c) }
        c.etMonto.doAfterTextChanged { pintarConversion(c) }
        pintarConversion(c)
    }

    private fun enDolares(c: Campos) = prefs.usarDolares && c.rgMoneda.checkedRadioButtonId == R.id.rbDolares

    private fun pintarConversion(c: Campos) {
        if (!enDolares(c)) {
            c.tvConversion.text = ""
            c.etMonto.hint = "Monto en quetzales"
            return
        }
        c.etMonto.hint = "Monto en dólares"
        val tasa = prefs.tasaDolar
        val dolares = c.etMonto.text.toString().trim().replace(',', '.').toDoubleOrNull()
        c.tvConversion.text = when {
            tasa <= 0 -> "Sin tipo de cambio. Revisa tu internet en Ajustes."
            dolares == null -> String.format(Locale.US, "Tipo de cambio de hoy: 1 USD = Q %.2f", tasa)
            else -> String.format(Locale.US, "US$ %,.2f = Q %,.2f", dolares, dolares * tasa)
        }
    }

    // Tocar la fecha, abre el calendario
    private fun configurarFecha(c: Campos) {
        c.tvFecha.text = "📅  ${Estilo.fechaLarga(c.fecha)}"
        c.tvFecha.setOnClickListener {
            val anio = c.fecha.substring(0, 4).toInt()
            val mes = c.fecha.substring(5, 7).toInt() - 1 // ojo: el calendario cuenta los meses desde 0
            val dia = c.fecha.substring(8, 10).toInt()
            DatePickerDialog(activity, { _, a, m, d ->
                c.fecha = "$a-${(m + 1).toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
                c.tvFecha.text = "📅  ${Estilo.fechaLarga(c.fecha)}"
            }, anio, mes, dia).show()
        }
    }

    // Modo editar: lleno los campos con lo que ya tenía y muestro el botón Eliminar
    private fun llenarParaEditar(c: Campos, editando: Any, hoja: BottomSheetDialog) {
        c.titulo.text = "Editar movimiento"
        c.btnGuardar.text = "Guardar cambios"
        c.btnEliminar.visibility = View.VISIBLE
        if (editando is Ingreso) c.rbIngreso.isChecked = true
        cargarSpinner(c)
        when (editando) {
            is Gasto -> {
                c.etDescripcion.setText(ConversionDolares.sinNota(editando.descripcion))
                c.etMonto.setText(Estilo.montoParaEditar(editando.monto))
                c.spOpcion.setSelection(c.categorias.indexOf(editando.categoria).coerceAtLeast(0))
            }
            is Ingreso -> {
                c.etDescripcion.setText(ConversionDolares.sinNota(editando.descripcion))
                c.etMonto.setText(Estilo.montoParaEditar(editando.monto))
                c.spOpcion.setSelection(TipoIngreso.entries.indexOf(editando.tipo))
            }
        }
        c.btnEliminar.setOnClickListener {
            DialogoEliminar.confirmar(activity, editando) { fecha ->
                hoja.dismiss()
                alEliminar(fecha)
            }
        }
    }

    private fun guardar(c: Campos, editando: Any?, hoja: BottomSheetDialog) {
        val seleccion = c.spOpcion.selectedItem ?: return
        val montoEscrito = ConversionDolares.leerMonto(c.etMonto.text.toString())
        val dolares = enDolares(c)
        if (dolares && prefs.tasaDolar <= 0) {
            Toast.makeText(activity, "No hay tipo de cambio. Revisa tu internet en Ajustes.", Toast.LENGTH_LONG).show()
            return
        }
        // La librería guarda todo en quetzales, si escriben en dólares lo convierte y lo anota
        val monto = if (dolares) ConversionDolares.aQuetzales(montoEscrito, prefs.tasaDolar) else montoEscrito
        val texto = c.etDescripcion.text.toString().trim()
        val descripcion = if (dolares) ConversionDolares.conNota(texto, montoEscrito) else texto
        val fecha = c.fecha

        activity.lifecycleScope.launch {
            val error = GuardadoMovimiento(Datos.control).guardar(editando, descripcion, monto, seleccion, fecha)
            if (error != null) {
                Toast.makeText(activity, error, Toast.LENGTH_SHORT).show()
                return@launch
            }
            hoja.dismiss()
            alGuardar(fecha, seleccion as? Categoria, editando != null)
        }
    }
}