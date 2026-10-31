package edu.umg.programacion2.appgastos.ui.ajustes

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.seguridad.PantallaProtegida
import edu.umg.programacion2.appgastos.ui.comun.Pantallas

// Pantalla de Ajustes.
// Cada tarjeta es una clase aparte que se encarga solo de lo suyo:
//  SeccionMoneda, SeccionCategorias, SeccionSeguridad, SeccionNotificaciones y SeccionPresupuestos.
// Todo lo que se elige se guarda en Preferencias (paquete datos).
class AjustesActivity : PantallaProtegida() {

    private lateinit var permiso: PermisoNotificaciones
    private lateinit var presupuestos: SeccionPresupuestos

    // Android exige registrar esto ANTES de que la pantalla arranque, por eso va aquí
    private val pedirPermiso =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            permiso.alResponder(concedido)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Pantallas.prepararPantallaOscura(this, R.layout.activity_ajustes)
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }

        permiso = PermisoNotificaciones(this, prefs, pedirPermiso)
        presupuestos = SeccionPresupuestos(this, prefs)
        SeccionMoneda(this, prefs).configurar()
        SeccionCategorias(this, prefs, alCambiar = { presupuestos.pintar() }).configurar()
        SeccionSeguridad(this, prefs).configurar()
        SeccionNotificaciones(this, prefs, permiso).configurar()
        presupuestos.pintar()
    }

    // Al regresar por ejemplo de los ajustes del teléfono, reviso otra vez el permiso
    override fun onResume() {
        super.onResume()
        permiso.revisar()
    }
}