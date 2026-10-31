package edu.umg.programacion2.appgastos.ui.principal

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.avisos.AvisosFinancieros
import edu.umg.programacion2.appgastos.avisos.Notificaciones
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.appgastos.logica.Periodo
import edu.umg.programacion2.appgastos.red.TipoCambio
import edu.umg.programacion2.appgastos.seguridad.PantallaBloqueo
import edu.umg.programacion2.appgastos.seguridad.PantallaProtegida
import edu.umg.programacion2.appgastos.ui.ajustes.AjustesActivity
import edu.umg.programacion2.appgastos.ui.comun.Estilo
import edu.umg.programacion2.appgastos.ui.comun.Pantallas
import edu.umg.programacion2.appgastos.ui.estadisticas.EstadisticasActivity
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.EstadoBalance
import edu.umg.programacion2.finanzas.servicio.fechaHoy
import edu.umg.programacion2.finanzas.servicio.mesDe
import kotlinx.coroutines.launch

// Pantalla principal.
// Solo coordina y conecta los botones con las piezas que hacen el trabajo.
// Los datos vienen de mi librería finanzas-core y se guardan en SQLite.
class MainActivity : PantallaProtegida() {

    private val control get() = Datos.control
    private val periodo = Periodo()
    private var categorias: List<Categoria> = Categoria.PREDETERMINADAS

    private lateinit var resumen: VistaResumen
    private lateinit var lista: ListaMovimientos
    private lateinit var hojaRegistro: HojaRegistro
    private lateinit var avisos: AvisosFinancieros

    private val pedirPermisoNotificaciones =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // PantallaProtegida abre la base y los ajustes
        Pantallas.prepararPantallaOscura(this, R.layout.activity_main)
        Notificaciones.crearCanales(this)
        pedirPermisoLaPrimeraVez()
        // Si el teléfono se reinició, Android borra las alarmas: vuelvo a programar el recordatorio
        if (prefs.recordatorioActivo) {
            Notificaciones.programarRecordatorio(this, prefs.horaRecordatorio, prefs.minutoRecordatorio)
        }

        resumen = VistaResumen(this)
        lista = ListaMovimientos(
            this,
            alTocar = { abrirRegistro(it) },
            alMantener = { DialogoEliminar.confirmar(this, it, ::alEliminar) }
        )
        hojaRegistro = HojaRegistro(this, prefs, alGuardar = ::alGuardar, alEliminar = ::alEliminar)
        avisos = AvisosFinancieros(this, prefs)
        conectarBotonesDeArriba()
        conectarBarraDeAbajo()
    }

    // Cada vez que regreso (por ejemplo de Ajustes) pinto con los ajustes nuevos
    override fun onResume() {
        super.onResume()
        Estilo.ocultarSaldos = prefs.ocultarSaldos
        refrescar()
        if (prefs.usarDolares) lifecycleScope.launch { TipoCambio.tasaDeHoy(prefs) }
    }

    // PantallaProtegida me avisa cuando hay que pedir el PIN
    override fun alNecesitarPin() {
        PantallaBloqueo(this, prefs, alBorrarTodo = ::empezarDeCero).mostrar()
    }

    // Le pido a la librería los datos del mes (o año) y se los paso a las piezas que pintan
    private fun refrescar() {
        lifecycleScope.launch {
            categorias = control.listarCategorias()
            val anio = periodo.porAnio
            val balance = if (anio) control.balanceDelAnio(periodo.anio) else control.balanceDelMes(periodo.mes)
            val ingresos = if (anio) control.ingresosDelAnio(periodo.anio) else control.ingresosDelMes(periodo.mes)
            val gastos = if (anio) control.gastosDelAnio(periodo.anio) else control.gastosDelMes(periodo.mes)
            resumen.pintar(periodo, balance)
            lista.pintar(anio, ingresos, gastos)
        }
    }

    private fun conectarBotonesDeArriba() {
        findViewById<View>(R.id.tvMes).setOnClickListener { SelectorMes(this, periodo, ::refrescar).mostrar() }
        findViewById<View>(R.id.btnAnterior).setOnClickListener { periodo.anterior(); refrescar() }
        findViewById<View>(R.id.btnSiguiente).setOnClickListener { periodo.siguiente(); refrescar() }
        findViewById<View>(R.id.btnPorMes).setOnClickListener { periodo.cambiarAPorMes(); refrescar() }
        findViewById<View>(R.id.btnPorAnio).setOnClickListener { periodo.cambiarAPorAnio(); refrescar() }
        // Tocar el balance lo oculta o lo muestra (privacidad)
        findViewById<View>(R.id.tvBalance).setOnClickListener {
            prefs.ocultarSaldos = !prefs.ocultarSaldos
            Estilo.ocultarSaldos = prefs.ocultarSaldos
            refrescar()
        }
    }

    private fun conectarBarraDeAbajo() {
        findViewById<View>(R.id.btnRegistrar).setOnClickListener { abrirRegistro(null) }
        findViewById<View>(R.id.navRegistrar).setOnClickListener { abrirRegistro(null) }
        findViewById<View>(R.id.navInicio).setOnClickListener {
            periodo.irAHoy()
            subirAlInicio()
            refrescar()
        }
        findViewById<View>(R.id.navHistorial).setOnClickListener {
            DialogoHistorial.mostrar(this) { mes ->
                periodo.verMes(mes)
                subirAlInicio()
                refrescar()
            }
        }
        findViewById<View>(R.id.navEstadisticas).setOnClickListener {
            startActivity(
                Intent(this, EstadisticasActivity::class.java)
                    .putExtra(EstadisticasActivity.EXTRA_MES, periodo.mesParaEstadisticas())
            )
        }
        findViewById<View>(R.id.navAjustes).setOnClickListener {
            startActivity(Intent(this, AjustesActivity::class.java))
        }
    }

    private fun abrirRegistro(editando: Any?) {
        hojaRegistro.abrir(editando, periodo.fechaPorDefecto(fechaHoy()), categorias)
    }

    // Después de guardar: salto a esa fecha, refresco y reviso los avisos de dinero
    private fun alGuardar(fecha: String, categoria: Categoria?, esEdicion: Boolean) {
        periodo.irAFecha(fecha)
        refrescar()
        lifecycleScope.launch {
            categoria?.let { avisos.revisarPresupuesto(it, mesDe(fecha)) }
            avisos.revisarBalanceNegativo(mesDe(fecha))
            val balance = if (periodo.porAnio) control.balanceDelAnio(periodo.anio) else control.balanceDelMes(periodo.mes)
            when {
                balance.estado == EstadoBalance.NEGATIVO ->
                    Toast.makeText(this@MainActivity, "¡Vamos mal! Gastas más de lo que ingresa", Toast.LENGTH_LONG).show()
                esEdicion -> Toast.makeText(this@MainActivity, "Cambios guardados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Después de borrar: refresco y reviso si el mes quedó en negativo (si borré un ingreso)
    private fun alEliminar(fecha: String) {
        refrescar()
        lifecycleScope.launch { avisos.revisarBalanceNegativo(mesDe(fecha)) }
    }

    // "Empezar de cero" desde la pantalla de bloqueo: borra todo y deja la app como nueva
    private fun empezarDeCero() {
        Notificaciones.cancelarRecordatorio(this)
        prefs.borrarTodo()
        Datos.borrarTodo(this)
        Estilo.ocultarSaldos = false
        periodo.reiniciar()
        refrescar()
    }

    private fun subirAlInicio() {
        findViewById<ScrollView>(R.id.scroll).smoothScrollTo(0, 0)
    }

    // Pido permiso de notificaciones una sola vez (Android 13+), para los avisos de ¡Vamos mal!
    private fun pedirPermisoLaPrimeraVez() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !Notificaciones.tienePermiso(this) && !prefs.yaPidioPermiso
        ) {
            prefs.yaPidioPermiso = true
            pedirPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}