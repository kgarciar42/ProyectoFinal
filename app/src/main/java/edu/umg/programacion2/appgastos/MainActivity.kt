package edu.umg.programacion2.appgastos

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.finanzas.datos.GastoRepositoryEnMemoria
import edu.umg.programacion2.finanzas.datos.IngresoRepositoryEnMemoria
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.EstadoBalance
import edu.umg.programacion2.finanzas.modelo.Gasto
import edu.umg.programacion2.finanzas.modelo.Ingreso
import edu.umg.programacion2.finanzas.modelo.TipoIngreso
import edu.umg.programacion2.finanzas.servicio.ControlGastos
import edu.umg.programacion2.finanzas.servicio.fechaHoy
import edu.umg.programacion2.finanzas.servicio.formatearQuetzales
import edu.umg.programacion2.finanzas.servicio.mesActual
import edu.umg.programacion2.finanzas.servicio.mesAnterior
import edu.umg.programacion2.finanzas.servicio.mesSiguiente
import edu.umg.programacion2.finanzas.servicio.nombreMes
import kotlinx.coroutines.launch

// La pantalla principal. Toda la lógica validar, balance, meses viene de mi librería finanzas-core
// aquí solo leo lo que escribe el usuario y muestro los resultados.
class MainActivity : AppCompatActivity() {

    companion object {
        // Lo puse en el companion object para que no se borre cuando giro el teléfono
        // Al girarlo Android vuelve a crear la pantalla.
        // si cierro la app se pierde todo. Después lo paso a Room.
        private val control = ControlGastos(GastoRepositoryEnMemoria(), IngresoRepositoryEnMemoria())
    }

    // El mes que estoy viendo. Arranca en el mes actual.
    private var mes = mesActual()

    private lateinit var tvMes: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvIngresos: TextView
    private lateinit var tvGastos: TextView
    private lateinit var tvMovimientos: TextView
    private lateinit var rgTipo: RadioGroup
    private lateinit var spOpcion: Spinner
    private lateinit var etDescripcion: EditText
    private lateinit var etMonto: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Esto es para que la pantalla no quede tapada por la barra de arriba del teléfono.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        // 1. Conecto cada variable con su vista del XML por el id.
        tvMes = findViewById(R.id.tvMes)
        tvBalance = findViewById(R.id.tvBalance)
        tvEstado = findViewById(R.id.tvEstado)
        tvIngresos = findViewById(R.id.tvIngresos)
        tvGastos = findViewById(R.id.tvGastos)
        tvMovimientos = findViewById(R.id.tvMovimientos)
        rgTipo = findViewById(R.id.rgTipo)
        spOpcion = findViewById(R.id.spOpcion)
        etDescripcion = findViewById(R.id.etDescripcion)
        etMonto = findViewById(R.id.etMonto)

        // 2. Lo que pasa cuando toco cada cosa.
        // Si cambio entre Gasto e Ingreso, cambio las opciones del Spinner.
        rgTipo.setOnCheckedChangeListener { _, _ -> cargarSpinner() }
        findViewById<Button>(R.id.btnGuardar).setOnClickListener { guardar() }
        findViewById<Button>(R.id.btnAnterior).setOnClickListener {
            mes = mesAnterior(mes)
            refrescar()
        }
        findViewById<Button>(R.id.btnSiguiente).setOnClickListener {
            mes = mesSiguiente(mes)
            refrescar()
        }

        // 3. Cómo arranca la pantalla.
        cargarSpinner()
        refrescar()
    }

    // Si está marcado Gasto, muestro las categorías, si es Ingreso, Salario, Ventas, etc.
    private fun cargarSpinner() {
        val opciones: List<Any> =
            if (rgTipo.checkedRadioButtonId == R.id.rbIngreso) TipoIngreso.entries else Categoria.entries
        spOpcion.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)
    }

    private fun guardar() {
        val descripcion = etDescripcion.text.toString().trim()
        // Si no escriben un número lo dejo en 0 y la librería me devuelve el error solita.
        val monto = etMonto.text.toString().trim().replace(',', '.').toDoubleOrNull() ?: 0.0
        // Si estoy en el mes actual uso la fecha de hoy; si estoy viendo otro mes, el día 1 de ese mes.
        val fecha = if (mes == mesActual()) fechaHoy() else "$mes-01"
        val seleccion = spOpcion.selectedItem

        lifecycleScope.launch {
            // Si lo que eligieron es un TipoIngreso es un ingreso; si no, es un gasto.
            val error = if (seleccion is TipoIngreso) {
                control.registrarIngreso(
                    Ingreso(descripcion = descripcion, monto = monto, tipo = seleccion, fecha = fecha)
                )
            } else {
                control.registrar(
                    Gasto(descripcion = descripcion, monto = monto, categoria = seleccion as Categoria, fecha = fecha)
                )
            }

            if (error != null) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Si se guardó, limpio los campos y actualizo la pantalla.
            etDescripcion.text.clear()
            etMonto.text.clear()
            refrescar()

            // Si con esto quedé en negativo, avisa.
            if (control.balanceDelMes(mes).estado == EstadoBalance.NEGATIVO) {
                Toast.makeText(this@MainActivity, "¡Vamos mal! Gastas más de lo que ingresa", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Le pido a la librería el balance del mes y lo pongo en pantalla.
    private fun refrescar() {
        lifecycleScope.launch {
            val balance = control.balanceDelMes(mes)

            tvMes.text = nombreMes(mes)
            tvBalance.text = formatearQuetzales(balance.saldo)
            tvEstado.text = balance.estado.mensaje
            tvIngresos.text = "Ingresos: ${formatearQuetzales(balance.totalIngresos)}"
            tvGastos.text = "Gastos: ${formatearQuetzales(balance.totalGastos)}"

            // El color según cómo voy: rojo si voy mal, naranja si voy mas o menos, verde si voy bien.
            val color = when (balance.estado) {
                EstadoBalance.NEGATIVO -> Color.parseColor("#C62828")
                EstadoBalance.CUIDADO -> Color.parseColor("#E65100")
                EstadoBalance.POSITIVO -> Color.parseColor("#2E7D32")
                EstadoBalance.SIN_MOVIMIENTOS -> Color.GRAY
            }
            tvBalance.setTextColor(color)
            tvEstado.setTextColor(color)

            // Armo la lista de movimientos del mes, + para ingresos y - para gastos.
            val texto = StringBuilder()
            for (ingreso in control.ingresosDelMes(mes)) {
                texto.append("+ ${ingreso.fecha}  ${ingreso.descripcion} (${ingreso.tipo})  ${formatearQuetzales(ingreso.monto)}\n")
            }
            for (gasto in control.gastosDelMes(mes)) {
                texto.append("- ${gasto.fecha}  ${gasto.descripcion} (${gasto.categoria})  ${formatearQuetzales(gasto.monto)}\n")
            }
            tvMovimientos.text = if (texto.isEmpty()) "Sin movimientos. Mes nuevo, control nuevo." else texto.toString()
        }
    }
}