package edu.umg.programacion2.appgastos.seguridad

import edu.umg.programacion2.appgastos.datos.Preferencias

// Lleva el registro de si ya puse el PIN .
// visibles cuenta, cuántas pantallas mías están abiertas, si llega a 0 es que salí de la app.
object Sesion {

    private var visibles = 0
    var desbloqueado = false

    fun pantallaVisible() {
        visibles++
    }

    // cambiandoConfiguracion = true, cuando solo giré el teléfono ahí no hay que pedir PIN
    fun pantallaOculta(cambiandoConfiguracion: Boolean) {
        visibles--
        if (visibles <= 0 && !cambiandoConfiguracion) desbloqueado = false
    }

    // Si no tengo PIN, nunca lo va a pedir.
    fun debePedirPin(prefs: Preferencias): Boolean {
        if (!prefs.tienePin) {
            desbloqueado = true
            return false
        }
        return !desbloqueado
    }
}