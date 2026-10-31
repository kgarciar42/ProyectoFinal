package edu.umg.programacion2.appgastos.seguridad

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.umg.programacion2.appgastos.datos.Datos
import edu.umg.programacion2.appgastos.datos.Preferencias

// Clase base de TODAS mis pantallas, se aplica herencia.
// abrir la base de datos y los ajustes
// revisar si hay que pedir el PIN al entrar y olvidar el PIN al salir de la app
abstract class PantallaProtegida : AppCompatActivity() {

    lateinit var prefs: Preferencias
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Datos.iniciar(this)
        prefs = Preferencias(this)
    }

    override fun onStart() {
        super.onStart()
        Sesion.pantallaVisible()
        if (Sesion.debePedirPin(prefs)) alNecesitarPin()
    }

    override fun onStop() {
        super.onStop()
        Sesion.pantallaOculta(isChangingConfigurations)
    }

    // Por defecto me regreso a la pantalla principal, que es la que muestra el bloqueo.
    // MainActivity cambia esto para mostrar la pantalla del PIN.
    protected open fun alNecesitarPin() {
        finish()
    }
}