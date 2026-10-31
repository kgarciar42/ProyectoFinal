package edu.umg.programacion2.appgastos.ui.estadisticas

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.appgastos.seguridad.PantallaProtegida
import edu.umg.programacion2.appgastos.ui.comun.Pantallas
import edu.umg.programacion2.finanzas.servicio.mesActual
import edu.umg.programacion2.finanzas.servicio.mesAnterior
import edu.umg.programacion2.finanzas.servicio.mesSiguiente
import edu.umg.programacion2.finanzas.servicio.nombreMes
import kotlinx.coroutines.launch

// Pantalla de Estadísticas.
// Solo coordina las 3 tarjetas
// Las cuentas están en logica/CalculosEstadisticas con pruebas.
class EstadisticasActivity : PantallaProtegida() {

    companion object {
        // El mes que estaba viendo en la pantalla principal, ej. "2026-10"
        const val EXTRA_MES = "mes"
    }

    private lateinit var mes: String
    private lateinit var barras: TarjetaBarras
    private lateinit var comparacion: TarjetaComparacion
    private lateinit var proyeccion: TarjetaProyeccion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Pantallas.prepararPantallaOscura(this, R.layout.activity_estadisticas)
        mes = intent.getStringExtra(EXTRA_MES) ?: mesActual()

        barras = TarjetaBarras(this, alTocarMes = { irA(it) })
        comparacion = TarjetaComparacion(this)
        proyeccion = TarjetaProyeccion(this)

        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }
        findViewById<View>(R.id.btnMesAnterior).setOnClickListener { irA(mesAnterior(mes)) }
        findViewById<View>(R.id.btnMesSiguiente).setOnClickListener { irA(mesSiguiente(mes)) }
        pintar()
    }

    private fun irA(nuevoMes: String) {
        mes = nuevoMes
        pintar()
    }

    private fun pintar() {
        val mesAPintar = mes // lo guardo para que las 3 tarjetas usen el mismo mes aunque toque rápido ‹ ›
        findViewById<TextView>(R.id.tvMesReferencia).text = nombreMes(mesAPintar)
        lifecycleScope.launch {
            val control = Datos.control
            val balance = control.balanceDelMes(mesAPintar)
            barras.pintar(mesAPintar, control)
            comparacion.pintar(balance, control.balanceDelMes(mesAnterior(mesAPintar)), mesAPintar)
            proyeccion.pintar(balance, mesAPintar)
        }
    }
}